package eu.jsparrow.core.visitor.unused.type;

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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;
import eu.jsparrow.core.visitor.unused.JavaElementSearchEngine;
import eu.jsparrow.core.visitor.utils.SearchScopeFactory;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

public class UnusedTypesEngine {

	private static final Logger logger = LoggerFactory.getLogger(UnusedTypesEngine.class);

	private String scope;
	private Set<ICompilationUnit> targetCompilationUnits = new HashSet<>();

	public UnusedTypesEngine(String scope) {
		this.scope = scope;
	}

	public List<UnusedTypeWrapper> findUnusedTypes(List<ICompilationUnit> selectedJavaElements,
			Map<String, Boolean> optionsMap, SubMonitor subMonitor) {

		Map<IPath, CompilationUnit> cache = new HashMap<>();
		List<UnusedTypeWrapper> list = new ArrayList<>();
		for (ICompilationUnit icu : selectedJavaElements) {
			CompilationUnit compilationUnit = RefactoringUtil.parse(icu);
			cache.put(icu.getPath(), compilationUnit);

			UnusedTypesCandidatesVisitor visitor = new UnusedTypesCandidatesVisitor(optionsMap);
			compilationUnit.accept(visitor);
			List<UnusedTypeWrapper> unusedPrivateMethods = visitor.getUnusedPrivateTypes();
			visitor.getUnusedLocalTypes();
			if (!unusedPrivateMethods.isEmpty()) {
				list.addAll(unusedPrivateMethods);
				targetCompilationUnits.add(icu);
			}

			List<NonPrivateUnusedTypeCandidate> nonPrivateCandidates = visitor.getNonPrivateCandidates();
			List<UnusedTypeWrapper> nonPrivate = filterUnusedTypes(compilationUnit, nonPrivateCandidates, cache);
			if (!nonPrivate.isEmpty()) {
				targetCompilationUnits.add(icu);
			}

			/*
			 * In case we want to support test references, here we have to
			 * analyze whether the external reference is inside a test case or
			 * not.
			 */

			list.addAll(nonPrivate);
			if (subMonitor.isCanceled()) {
				logger.debug("Cancelled while searching for unused methods."); //$NON-NLS-1$
				return Collections.emptyList();
			} else {
				subMonitor.worked(1);
			}
		}
		return list;
	}

	private List<UnusedTypeWrapper> filterUnusedTypes(CompilationUnit compilationUnit,
			List<NonPrivateUnusedTypeCandidate> nonPrivateCandidates, Map<IPath, CompilationUnit> cache) {
		List<UnusedTypeWrapper> list = new ArrayList<>();
		IJavaElement javaElement = compilationUnit.getJavaElement();
		IJavaProject javaProject = javaElement.getJavaProject();
		for (NonPrivateUnusedTypeCandidate candidate : nonPrivateCandidates) {
			AbstractTypeDeclaration typeDeclaration = candidate.getTypeDeclaration();
			UnusedTypeReferenceSearchResult searchResult = searchReferences(typeDeclaration, javaProject, cache);
			if (!searchResult.isMainSourceReferenceFound()
					&& !searchResult.isInvalidSearchEngineResult()) {
				UnusedTypeWrapper unusedMethodWrapper = new UnusedTypeWrapper(compilationUnit,
						candidate.getAccessModifier(), typeDeclaration);
				list.add(unusedMethodWrapper);
			}
		}
		return list;
	}

	private UnusedTypeReferenceSearchResult searchReferences(AbstractTypeDeclaration typeDeclaration,
			IJavaProject project,
			Map<IPath, CompilationUnit> cache) {
		CompilationUnit compilationUnitEnclosingType = ASTNodeUtil.getSpecificAncestor(typeDeclaration,
				CompilationUnit.class);
		if(compilationUnitEnclosingType == null) {
			return new UnusedTypeReferenceSearchResult(false, true);
		}
		IJavaElement[] searchScope = createSearchScope(scope, project);
		JavaElementSearchEngine referencesSearchEngine = new JavaElementSearchEngine(searchScope);
		SimpleName name = typeDeclaration.getName();
		String methodIdentifier = name.getIdentifier();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		if (typeBinding == null) {
			return new UnusedTypeReferenceSearchResult(false, true);
		}
		IJavaElement javaElement = typeBinding.getJavaElement();
		SearchPattern searchPattern = SearchPattern.createPattern(javaElement, IJavaSearchConstants.REFERENCES);
		Optional<List<ReferenceSearchMatch>> references = referencesSearchEngine.findReferences(searchPattern,
				methodIdentifier);
		if (!references.isPresent()) {
			return new UnusedTypeReferenceSearchResult(false, true);
		}

		Set<ICompilationUnit> targetICUs = referencesSearchEngine.getTargetIJavaElements();

		for (ICompilationUnit iCompilationUnit : targetICUs) {
			IPath path = iCompilationUnit.getPath();
			CompilationUnit compilationUnit = cache.computeIfAbsent(path,
					iPath -> RefactoringUtil.parse(iCompilationUnit));
			TypeReferencesVisitor visitor = new TypeReferencesVisitor(typeDeclaration, compilationUnitEnclosingType);
			compilationUnit.accept(visitor);
			if (visitor.typeReferenceFound() || visitor.hasUnresolvedReference()) {
				return new UnusedTypeReferenceSearchResult(true, false);
			}
		}
		return new UnusedTypeReferenceSearchResult(false, false);
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
