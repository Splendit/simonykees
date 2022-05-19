package eu.jsparrow.core.visitor.unused.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.rule.impl.unused.Constants;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;
import eu.jsparrow.core.visitor.unused.JavaElementSearchEngine;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodWrapper;
import eu.jsparrow.core.visitor.utils.SearchScopeFactory;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.RefactoringUtil;

/**
 * An engine to find unused types.
 * 
 * @since 4.10.0
 *
 */
public class UnusedTypesEngine {

	private static final Logger logger = LoggerFactory.getLogger(UnusedTypesEngine.class);

	private String scope;
	private Set<ICompilationUnit> targetCompilationUnits = new HashSet<>();

	public UnusedTypesEngine(String scope) {
		this.scope = scope;
	}

	public List<UnusedTypeWrapper> findUnusedTypes(List<ICompilationUnit> selectedJavaElements,
			Map<String, Boolean> optionsMap, SubMonitor subMonitor, List<UnusedMethodWrapper> unusedMethods) {

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
				List<UnusedTypeWrapper> filtered = filterRemovedWithUnusedMethods(unusedLocalTypes, unusedMethods);
				list.addAll(filtered);
				targetCompilationUnits.add(icu);
			}

			List<NonPrivateUnusedTypeCandidate> nonPrivateCandidates = visitor.getNonPrivateCandidates();
			List<UnusedTypeWrapper> nonPrivate = filterUnusedTypes(compilationUnit, nonPrivateCandidates, cache,
					optionsMap);
			List<UnusedTypeWrapper> overlappings = this.findTestOverlappings(list, nonPrivate);
			nonPrivate.removeAll(overlappings);
			if (!nonPrivate.isEmpty()) {
				targetCompilationUnits.add(icu);
			}

			for (UnusedTypeWrapper unusedNonPrivateType : nonPrivate) {
				List<TestReferenceOnType> tests = unusedNonPrivateType.getTestReferencesOnType();
				for (TestReferenceOnType test : tests) {
					targetCompilationUnits.add(test.getICompilationUnit());
				}
			}

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

	private List<UnusedTypeWrapper> filterRemovedWithUnusedMethods(List<UnusedTypeWrapper> unusedLocalTypes,
			List<UnusedMethodWrapper> unusedMethods) {
		List<UnusedTypeWrapper> filtered = new ArrayList<>();
		for (UnusedTypeWrapper unusedType : unusedLocalTypes) {
			AbstractTypeDeclaration typeDeclaration = unusedType.getTypeDeclaration();
			MethodDeclaration outerMethod = ASTNodeUtil.getSpecificAncestor(typeDeclaration, MethodDeclaration.class);
			if (outerMethod == null) {
				filtered.add(unusedType);
			} else {
				IMethodBinding outerMethodBinding = outerMethod.resolveBinding();
				boolean match = unusedMethods.stream()
					.map(UnusedMethodWrapper::getMethodDeclaration)
					.map(MethodDeclaration::resolveBinding)
					.anyMatch(binding -> binding.isEqualTo(outerMethodBinding));
				if (!match) {
					filtered.add(unusedType);
				}
			}
		}
		return filtered;
	}

	private List<UnusedTypeWrapper> filterUnusedTypes(CompilationUnit compilationUnit,
			List<NonPrivateUnusedTypeCandidate> nonPrivateCandidates, Map<IPath, CompilationUnit> cache,
			Map<String, Boolean> optionsMap) {
		List<UnusedTypeWrapper> list = new ArrayList<>();
		IJavaElement javaElement = compilationUnit.getJavaElement();
		IJavaProject javaProject = javaElement.getJavaProject();
		for (NonPrivateUnusedTypeCandidate candidate : nonPrivateCandidates) {

			AbstractTypeDeclaration typeDeclaration = candidate.getTypeDeclaration();
			UnusedTypeReferenceSearchResult searchResult = searchReferences(typeDeclaration, javaProject, cache,
					optionsMap);
			if (!searchResult.isMainSourceReferenceFound()
					&& !searchResult.isInvalidSearchEngineResult()) {
				List<TestReferenceOnType> testReferencesOnType = searchResult.getTestReferencesOnType();
				UnusedTypeWrapper unusedTypeWrapper = new UnusedTypeWrapper(compilationUnit,
						candidate.getAccessModifier(), typeDeclaration, candidate.isMainType(),
						testReferencesOnType);
				if (!hasOverlapping(list, testReferencesOnType)) {
					list.add(unusedTypeWrapper);
				}
			}
		}
		return list;
	}

	private boolean hasOverlapping(List<UnusedTypeWrapper> list, List<TestReferenceOnType> testReferencesOnType) {
		for (TestReferenceOnType testReference : testReferencesOnType) {
			Set<MethodDeclaration> tests = testReference.getTestMethodsReferencingType();
			boolean overlappingTest = list.stream()
				.flatMap(unusedType -> unusedType.getTestReferencesOnType()
					.stream())
				.flatMap(testReferenceWrapper -> testReferenceWrapper.getTestMethodsReferencingType()
					.stream())
				.anyMatch(tests::contains);
			if (overlappingTest) {
				return true;
			}

			List<AbstractTypeDeclaration> types = testReference.getTestTypesReferencingType()
				.stream()
				.flatMap(type -> findAllEnclosingTypes(type).stream())
				.collect(Collectors.toList());

			boolean overlappingTestTypes = list.stream()
				.flatMap(unusedType -> unusedType.getTestReferencesOnType()
					.stream())
				.flatMap(testReferenceWrapper -> testReferenceWrapper.getTestTypesReferencingType()
					.stream())
				.anyMatch(types::contains);
			if (overlappingTestTypes) {
				return true;
			}
		}
		return false;
	}

	public List<UnusedTypeWrapper> findTestOverlappings(List<UnusedTypeWrapper> allUnusedTypes,
			List<UnusedTypeWrapper> newUnusedTypes) {
		List<UnusedTypeWrapper> overlappings = new ArrayList<>();
		for (UnusedTypeWrapper newUnusedType : newUnusedTypes) {
			List<TestReferenceOnType> testReferences = newUnusedType.getTestReferencesOnType();
			if (hasOverlapping(allUnusedTypes, testReferences)) {
				overlappings.add(newUnusedType);
			}
		}
		return overlappings;
	}

	private List<AbstractTypeDeclaration> findAllEnclosingTypes(AbstractTypeDeclaration type) {
		List<AbstractTypeDeclaration> types = new ArrayList<>();
		ASTNode node = type;
		while (true) {
			AbstractTypeDeclaration enclosing = ASTNodeUtil.getSpecificAncestor(node, AbstractTypeDeclaration.class);
			if (enclosing == null) {
				break;
			}
			types.add(enclosing);
			node = enclosing;
		}
		return types;
	}

	private UnusedTypeReferenceSearchResult searchReferences(
			AbstractTypeDeclaration typeDeclaration,
			IJavaProject project,
			Map<IPath, CompilationUnit> cache,
			Map<String, Boolean> optionsMap) {

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
			CompilationUnit compilationUnit = cache.computeIfAbsent(path,
					iPath -> RefactoringUtil.parse(iCompilationUnit));

			JUnitTestMethodVisitor testMethodVisitor = new JUnitTestMethodVisitor();

			compilationUnit.accept(testMethodVisitor);

			if (testMethodVisitor.isJUnitTestCaseFound()) {
				TestReferenceOnType referenceOnTest = createReferenceOnTestInstance(typeBinding, compilationUnit,
						iCompilationUnit);
				testReferencesOnType.add(referenceOnTest);
			} else {
				return new UnusedTypeReferenceSearchResult(true, false);
			}
		}
		return new UnusedTypeReferenceSearchResult(false, false, testReferencesOnType);

	}

	private TestReferenceOnType createReferenceOnTestInstance(ITypeBinding typeBinding, CompilationUnit compilationUnit,
			ICompilationUnit iCompilationUnit) {
		String typeName = typeBinding.getErasure()
			.getQualifiedName();
		ReferencesInTestAnalyzerVisitor visitor = new ReferencesInTestAnalyzerVisitor(typeName);
		compilationUnit.accept(visitor);

		if (visitor.isMainTopLevelTypeDesignated()) {
			return new TestReferenceOnType(compilationUnit, iCompilationUnit, true,
					visitor.getTypesWithReferencesToUnusedType(), Collections.emptySet(), Collections.emptySet());
		}

		Set<MethodDeclaration> testCases = visitor.getTestMethodsHavingUnusedTypeReferences();
		Set<AbstractTypeDeclaration> types = visitor.getTypesWithReferencesToUnusedType();
		Set<ImportDeclaration> imports = visitor.getUnusedTypeImports();

		return new TestReferenceOnType(compilationUnit, iCompilationUnit, false, types, testCases, imports);
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
