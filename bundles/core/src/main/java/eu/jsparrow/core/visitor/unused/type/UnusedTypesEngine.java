package eu.jsparrow.core.visitor.unused.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;
import eu.jsparrow.core.visitor.unused.JavaElementSearchEngine;
import eu.jsparrow.core.visitor.utils.SearchScopeFactory;
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
			List<UnusedTypeWrapper> unusedPrivateTypes = visitor.getUnusedPrivateTypes();
			visitor.getUnusedLocalTypes();
			if (!unusedPrivateTypes.isEmpty()) {
				list.addAll(unusedPrivateTypes);
				targetCompilationUnits.add(icu);
			}

			List<UnusedTypeWrapper> unusedLocalTypes = visitor.getUnusedLocalTypes();
			if (!unusedLocalTypes.isEmpty()) {
				list.addAll(unusedLocalTypes);
				targetCompilationUnits.add(icu);
			}

			List<NonPrivateUnusedTypeCandidate> nonPrivateCandidates = visitor.getNonPrivateCandidates();
			List<UnusedTypeWrapper> nonPrivate = filterUnusedTypes(compilationUnit, nonPrivateCandidates, cache,
					optionsMap);
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
			List<NonPrivateUnusedTypeCandidate> nonPrivateCandidates, Map<IPath, CompilationUnit> cache,
			Map<String, Boolean> optionsMap) {
		List<UnusedTypeWrapper> list = new ArrayList<>();
		IJavaElement javaElement = compilationUnit.getJavaElement();
		IJavaProject javaProject = javaElement.getJavaProject();
		List<IPath> testReferencePathsAlreadyUsed = new ArrayList<>();
		for (NonPrivateUnusedTypeCandidate candidate : nonPrivateCandidates) {

			AbstractTypeDeclaration typeDeclaration = candidate.getTypeDeclaration();
			UnusedTypeReferenceSearchResult searchResult = searchReferences(typeDeclaration, javaProject, cache,
					optionsMap, testReferencePathsAlreadyUsed);
			if (!searchResult.isMainSourceReferenceFound()
					&& !searchResult.isInvalidSearchEngineResult()) {
				List<TestReferenceOnType> testReferencesOnType = searchResult.getTestReferencesOnType();
				UnusedTypeWrapper unusedTypeWrapper = new UnusedTypeWrapper(compilationUnit,
						candidate.getAccessModifier(), typeDeclaration, candidate.isMainType(),
						testReferencesOnType);
				list.add(unusedTypeWrapper);
				if (!testReferencesOnType.isEmpty()) {
					testReferencesOnType.stream()
						.map(TestReferenceOnType::getICompilationUnit)
						.map(ICompilationUnit::getPath)
						.forEach(testReferencePathsAlreadyUsed::add);
				}
			}
		}
		return list;
	}

	private UnusedTypeReferenceSearchResult searchReferences(
			AbstractTypeDeclaration typeDeclaration,
			IJavaProject project,
			Map<IPath, CompilationUnit> cache,
			Map<String, Boolean> optionsMap,
			List<IPath> testReferencePathsAlreadyUsed) {

		IJavaElement[] searchScope = createSearchScope(scope, project);
		JavaElementSearchEngine referencesSearchEngine = new JavaElementSearchEngine(searchScope);
		SimpleName name = typeDeclaration.getName();
		String typeIdentifier = name.getIdentifier();
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();
		if (typeBinding == null) {
			return new UnusedTypeReferenceSearchResult(false, true);
		}
		IJavaElement javaElement = typeBinding.getJavaElement();
		SearchPattern searchPattern = SearchPattern.createPattern(javaElement, IJavaSearchConstants.REFERENCES);

		List<ReferenceSearchMatch> references = referencesSearchEngine.findReferences(searchPattern,
				typeIdentifier)
			.orElse(null);

		if (references == null) {
			return new UnusedTypeReferenceSearchResult(false, true);
		}
		if (references.isEmpty()) {
			return new UnusedTypeReferenceSearchResult(false, false);
		}

		boolean removeTestCodeOption = optionsMap.getOrDefault(Constants.REMOVE_TEST_CODE, false);
		if (!removeTestCodeOption) {
			return new UnusedTypeReferenceSearchResult(true, false);
		}

		Set<ICompilationUnit> targetICUs = referencesSearchEngine.getTargetIJavaElements();
		List<TestReferenceOnType> testReferencesOnType = new ArrayList<>();

		for (ICompilationUnit iCompilationUnit : targetICUs) {
			IPath path = iCompilationUnit.getPath();
			if (testReferencePathsAlreadyUsed.contains(path)) {
				return new UnusedTypeReferenceSearchResult(true, false);
			}
			CompilationUnit compilationUnit = cache.computeIfAbsent(path,
					iPath -> RefactoringUtil.parse(iCompilationUnit));

			JUnitTestMethodVisitor testMethodVisitor = new JUnitTestMethodVisitor();

			compilationUnit.accept(testMethodVisitor);

			if (testMethodVisitor.isJUnitTestCaseFound()) {
				testReferencesOnType.add(new TestReferenceOnType(compilationUnit, iCompilationUnit));
			} else {
				return new UnusedTypeReferenceSearchResult(true, false);
			}
		}
		return new UnusedTypeReferenceSearchResult(false, false, testReferencesOnType);

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
