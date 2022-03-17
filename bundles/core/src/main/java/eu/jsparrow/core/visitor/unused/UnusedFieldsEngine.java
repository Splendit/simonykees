package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;
import eu.jsparrow.core.visitor.utils.SearchScopeFactory;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

/**
 * An engine to search for unused fields. Uses
 * {@link UnusedFieldsCandidatesVisitor} to analyze field declarations. Uses
 * {@link JavaElementSearchEngine} to find the references of fields in external
 * files. Provides the results as a list of {@link UnusedFieldWrapper}s.
 * 
 * @since 4.8.0
 */
public class UnusedFieldsEngine {

	private static final Logger logger = LoggerFactory.getLogger(UnusedFieldsEngine.class);

	private String scope;
	private Set<ICompilationUnit> targetCompilationUnits = new HashSet<>();

	public UnusedFieldsEngine(String scope) {
		this.scope = scope;
	}

	public List<UnusedFieldWrapper> findUnusedFields(List<ICompilationUnit> selectedJavaElements,
			Map<String, Boolean> optionsMap, SubMonitor subMonitor) {

		List<UnusedFieldWrapper> list = new ArrayList<>();
		Map<IPath, CompilationUnit> cache = new HashMap<>();
		for (ICompilationUnit icu : selectedJavaElements) {
			CompilationUnit compilationUnit = RefactoringUtil.parse(icu);
			cache.put(icu.getPath(), compilationUnit);
			UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(optionsMap);
			compilationUnit.accept(visitor);
			List<UnusedFieldWrapper> unusedPrivateFields = visitor.getUnusedPrivateFields();
			if (!unusedPrivateFields.isEmpty()) {
				list.addAll(unusedPrivateFields);
				targetCompilationUnits.add(icu);
			}

			List<NonPrivateUnusedFieldCandidate> nonPrivateCandidates = visitor.getNonPrivateCandidates();
			List<UnusedFieldWrapper> nonPrivate = findExternalUnusedReferences(compilationUnit, nonPrivateCandidates,
					optionsMap, cache);
			if (!nonPrivate.isEmpty()) {
				targetCompilationUnits.add(icu);
			}

			for (UnusedFieldWrapper unused : nonPrivate) {
				List<UnusedExternalReferences> externalReferences = unused.getUnusedExternalReferences();
				for (UnusedExternalReferences r : externalReferences) {
					targetCompilationUnits.add((ICompilationUnit) r.getCompilationUnit()
						.getJavaElement());
				}
			}
			list.addAll(nonPrivate);
			if (subMonitor.isCanceled()) {
				logger.debug("Cancelled while searching for unused fields."); //$NON-NLS-1$
				return Collections.emptyList();
			} else {
				subMonitor.worked(1);
			}
		}
		return list;
	}

	private List<UnusedFieldWrapper> findExternalUnusedReferences(CompilationUnit compilationUnit,
			List<NonPrivateUnusedFieldCandidate> nonPrivateCandidates, Map<String, Boolean> optionsMap, Map<IPath, CompilationUnit>cache) {
		List<UnusedFieldWrapper> list = new ArrayList<>();
		IJavaElement javaElement = compilationUnit.getJavaElement();
		IJavaProject javaProject = javaElement.getJavaProject();
		for (NonPrivateUnusedFieldCandidate candidate : nonPrivateCandidates) {
			VariableDeclarationFragment fragment = candidate.getFragment();
			UnusedFieldReferenceSearchResult searchResult = searchReferences(fragment, javaProject, optionsMap, cache);
			if (!searchResult.isActiveReferenceFound() && !searchResult.isInvalidSearchEngineResult()) {
				List<UnusedExternalReferences> unusedReferences = searchResult.getUnusedReferences();
				List<ExpressionStatement> internalReassignments = candidate.getInternalReassignments();
				UnusedFieldWrapper unusedFieldWrapper = new UnusedFieldWrapper(compilationUnit,
						candidate.getAccessModifier(), fragment, internalReassignments, unusedReferences);
				list.add(unusedFieldWrapper);
			}
		}
		return list;
	}

	private UnusedFieldReferenceSearchResult searchReferences(VariableDeclarationFragment fragment,
			IJavaProject project,
			Map<String, Boolean> optionsMap, 
			Map<IPath, CompilationUnit> cache) {
		IJavaElement[] searchScope = createSearchScope(scope, project);
		JavaElementSearchEngine fieldReferencesSearchEngine = new JavaElementSearchEngine(searchScope);
		SearchPattern pattern = createSearchPattern(fragment);
		String identifier = fragment.getName()
			.getIdentifier();
		Optional<List<ReferenceSearchMatch>> references = fieldReferencesSearchEngine.findReferences(pattern,
				identifier);
		if (!references.isPresent()) {
			return new UnusedFieldReferenceSearchResult(false, true, Collections.emptyList());
		}
		Set<ICompilationUnit> targetICUs = fieldReferencesSearchEngine.getTargetIJavaElements();
		/*
		 * Make a cache with parsed compilation units. Keep all the icu-s in a
		 * targetCompilationUnits field.
		 */
		AbstractTypeDeclaration typDeclaration = ASTNodeUtil.getSpecificAncestor(fragment,
				AbstractTypeDeclaration.class);
		List<UnusedExternalReferences> unusedExternalreferences = new ArrayList<>();
		for (ICompilationUnit iCompilationUnit : targetICUs) {
			CompilationUnit compilationUnit = cache.computeIfAbsent(iCompilationUnit.getPath(), 
					path -> RefactoringUtil.parse(iCompilationUnit));
			ReferencesVisitor visitor = new ReferencesVisitor(fragment, typDeclaration, optionsMap);
			compilationUnit.accept(visitor);
			if (!visitor.hasActiveReference() && !visitor.hasUnresolvedReference()) {
				List<ExpressionStatement> reassignments = visitor.getReassignments();
				UnusedExternalReferences unusedReferences = new UnusedExternalReferences(compilationUnit,
						iCompilationUnit, reassignments);
				unusedExternalreferences.add(unusedReferences);
			} else {
				return new UnusedFieldReferenceSearchResult(true, false, Collections.emptyList());
			}
		}
		return new UnusedFieldReferenceSearchResult(false, false, unusedExternalreferences);
	}

	private SearchPattern createSearchPattern(VariableDeclarationFragment fragment) {
		IVariableBinding fragmentBinding = fragment.resolveBinding();
		IJavaElement iVariableBinding = fragmentBinding.getJavaElement();
		return SearchPattern.createPattern(iVariableBinding, IJavaSearchConstants.REFERENCES);
	}

	private IJavaElement[] createSearchScope(String modelSearchScope, IJavaProject javaProject) {
		if ("Project".equalsIgnoreCase(modelSearchScope)) { //$NON-NLS-1$
			return new IJavaElement[] { javaProject };
		}
		return SearchScopeFactory.createWorkspaceSearchScope();
	}

	public Set<ICompilationUnit> getTargetCompilationUnits() {
		return targetCompilationUnits;
	}
}
