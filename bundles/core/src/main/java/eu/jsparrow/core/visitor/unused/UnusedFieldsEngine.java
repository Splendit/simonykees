package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.renaming.FieldReferencesSearch;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;
import eu.jsparrow.core.visitor.utils.SearchScopeFactory;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

public class UnusedFieldsEngine {

	private static final Logger logger = LoggerFactory.getLogger(UnusedFieldsEngine.class);

	private String scope;
	private Set<ICompilationUnit> targetCompilationUnits = new HashSet<>();

	public UnusedFieldsEngine(String scope) {
		this.scope = scope;
	}

	public List<UnusedFieldWrapper> findUnusedFields(List<ICompilationUnit> selectedJavaElements,
			Map<String, Boolean> optionsMap, SubMonitor subMonitor) {
		List<CompilationUnit> compilationUnits = new ArrayList<>();
		List<UnusedFieldWrapper> list = new ArrayList<>();
		for (ICompilationUnit icu : selectedJavaElements) {
			CompilationUnit compilationUnit = RefactoringUtil.parse(icu);
			compilationUnits.add(compilationUnit);

			UnusedFieldsCandidatesVisitor visitor = new UnusedFieldsCandidatesVisitor(optionsMap);
			compilationUnit.accept(visitor);
			List<UnusedFieldWrapper> unusedPrivateFields = visitor.getUnusedPrivateFields();
			if (!unusedPrivateFields.isEmpty()) {
				list.addAll(unusedPrivateFields);
				targetCompilationUnits.add(icu);
			}

			List<NonPrivateUnusedFieldCandidate> nonPrivateCandidates = visitor.getNonPrivateCandidates();
			List<UnusedFieldWrapper> nonPrivate = findExternalUnusedReferences(compilationUnit, nonPrivateCandidates,
					optionsMap);
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
			List<NonPrivateUnusedFieldCandidate> nonPrivateCandidates, Map<String, Boolean> optionsMap) {
		List<UnusedFieldWrapper> list = new ArrayList<>();
		IJavaElement javaElement = compilationUnit.getJavaElement();
		IJavaProject javaProject = javaElement.getJavaProject();
		for (NonPrivateUnusedFieldCandidate candidate : nonPrivateCandidates) {
			VariableDeclarationFragment fragment = candidate.getFragment();
			UnusedFieldReferenceSearchResult searchResult = searchReferences(fragment, javaProject, optionsMap);
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
			Map<String, Boolean> optionsMap) {
		IJavaElement[] searchScope = createSearchScope(scope, project);
		FieldReferencesSearch fieldReferencesSearchEngine = new FieldReferencesSearch(searchScope);
		Optional<List<ReferenceSearchMatch>> references = fieldReferencesSearchEngine.findFieldReferences(fragment);
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
			CompilationUnit compilationUnit = RefactoringUtil.parse(iCompilationUnit);
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
