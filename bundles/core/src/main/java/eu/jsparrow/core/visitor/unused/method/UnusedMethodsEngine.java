package eu.jsparrow.core.visitor.unused.method;

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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
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

public class UnusedMethodsEngine {
	
	private static final Logger logger = LoggerFactory.getLogger(UnusedMethodsEngine.class);

	private String scope;
	private Set<ICompilationUnit> targetCompilationUnits = new HashSet<>();

	public UnusedMethodsEngine(String scope) {
		this.scope = scope;
	}


	public List<UnusedMethodWrapper> findUnusedMethods(List<ICompilationUnit> selectedJavaElements,
			Map<String, Boolean> optionsMap, SubMonitor subMonitor) {
		
		List<CompilationUnit> compilationUnits = new ArrayList<>();
		List<UnusedMethodWrapper> list = new ArrayList<>();
		for (ICompilationUnit icu : selectedJavaElements) {
			CompilationUnit compilationUnit = RefactoringUtil.parse(icu);
			compilationUnits.add(compilationUnit);

			UnusedMethodsCandidateVisitor visitor = new UnusedMethodsCandidateVisitor(optionsMap);
			compilationUnit.accept(visitor);
			List<UnusedMethodWrapper> unusedPrivateFields = visitor.getUnusedPrivateMethods();
			if (!unusedPrivateFields.isEmpty()) {
				list.addAll(unusedPrivateFields);
				targetCompilationUnits.add(icu);
			}

			List<NonPrivateUnusedMethodCandidate> nonPrivateCandidates = visitor.getNonPrivateCandidates();
			List<UnusedMethodWrapper> nonPrivate = findExternalUnusedReferences(compilationUnit, nonPrivateCandidates,
					optionsMap);
			if (!nonPrivate.isEmpty()) {
				targetCompilationUnits.add(icu);
			}


			for (UnusedMethodWrapper unused : nonPrivate) {
				List<TestSourceReference> externalReferences = unused.getTestReferences();
				for (TestSourceReference r : externalReferences) {
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
	
	private List<UnusedMethodWrapper> findExternalUnusedReferences(CompilationUnit compilationUnit,
			List<NonPrivateUnusedMethodCandidate> nonPrivateCandidates, Map<String, Boolean> optionsMap) {
		List<UnusedMethodWrapper> list = new ArrayList<>();
		IJavaElement javaElement = compilationUnit.getJavaElement();
		IJavaProject javaProject = javaElement.getJavaProject();
		for (NonPrivateUnusedMethodCandidate candidate : nonPrivateCandidates) {
			MethodDeclaration methodDeclaration = candidate.getDeclaration();
			UnusedMethodReferenceSearchResult searchResult = searchReferences(methodDeclaration, javaProject, optionsMap);
			if (!searchResult.isMainSourceReferenceFound() && !searchResult.isInvalidSearchEngineResult()) {
				List<TestSourceReference> testReferences = searchResult.getReferencesInTestSources();

				UnusedMethodWrapper unusedFieldWrapper = new UnusedMethodWrapper(compilationUnit,
						candidate.getAccessModifier(), methodDeclaration, testReferences);
				list.add(unusedFieldWrapper);
			}
		}
		return list;
	}
	
	private UnusedMethodReferenceSearchResult searchReferences(MethodDeclaration methodDeclaration,
			IJavaProject project,
			Map<String, Boolean> optionsMap) {
		IJavaElement[] searchScope = createSearchScope(scope, project);
		JavaElementSearchEngine fieldReferencesSearchEngine = new JavaElementSearchEngine(searchScope);
		SimpleName name = methodDeclaration.getName();
		String methodIdentifier = name.getIdentifier();
		SearchPattern searchPattern = createSearchPattern(methodDeclaration);
		Optional<List<ReferenceSearchMatch>> references = fieldReferencesSearchEngine.findFieldReferences(searchPattern, methodIdentifier);
		if (!references.isPresent()) {
			return new UnusedMethodReferenceSearchResult(false, true, Collections.emptyList());
		}
		Set<ICompilationUnit> targetICUs = fieldReferencesSearchEngine.getTargetIJavaElements();
		/*
		 * Make a cache with parsed compilation units. Keep all the icu-s in a
		 * targetCompilationUnits field.
		 */
		AbstractTypeDeclaration typDeclaration = ASTNodeUtil.getSpecificAncestor(methodDeclaration,
				AbstractTypeDeclaration.class);
		List<TestSourceReference> relatedTestDeclarations = new ArrayList<>();
		for (ICompilationUnit iCompilationUnit : targetICUs) {
			CompilationUnit compilationUnit = RefactoringUtil.parse(iCompilationUnit);
			
			MethodReferencesVisitor visitor = new MethodReferencesVisitor(methodDeclaration, typDeclaration, optionsMap);
			compilationUnit.accept(visitor);
			if (!visitor.hasMainSourceReference() && !visitor.hasUnresolvedReference()) {
				List<MethodDeclaration> testMethodDeclarations = visitor.getRelatedTestDeclarations();
				TestSourceReference unusedReferences = new TestSourceReference(compilationUnit,
						iCompilationUnit, testMethodDeclarations);
				relatedTestDeclarations.add(unusedReferences);
			} else {
				return new UnusedMethodReferenceSearchResult(true, false, Collections.emptyList());
			}
		}
		return new UnusedMethodReferenceSearchResult(false, false, relatedTestDeclarations);
	}

	private SearchPattern createSearchPattern(MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		IJavaElement javaElement = methodBinding.getJavaElement();
		return SearchPattern.createPattern(javaElement, IJavaSearchConstants.REFERENCES);
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
