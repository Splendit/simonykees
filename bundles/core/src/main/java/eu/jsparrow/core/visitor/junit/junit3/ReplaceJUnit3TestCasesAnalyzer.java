package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.junit.junit3.JUnit3ReferencesAnalyzerVisitor.isJUnit3QualifiedName;
import static eu.jsparrow.core.visitor.utils.MainMethodMatches.findMainMethodMatches;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.core.visitor.junit.jupiter.common.MethodInvocationsCollectorVisitor;
import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;

public class ReplaceJUnit3TestCasesAnalyzer {

	static final String SET_UP = "setUp"; //$NON-NLS-1$
	static final String TEAR_DOWN = "tearDown"; //$NON-NLS-1$
	static final String TEST = "test"; //$NON-NLS-1$

	public Optional<ReplaceJUnit3TestCasesAnalysisData> analyzeCompilationUnit(CompilationUnit compilationUnit,
			Junit3MigrationConfiguration migrationConfiguration, String classDeclaringMethodReplacement) {

		MethodDeclarationsCollectorVisitor methodDeclarationsCollectorVisitor = new MethodDeclarationsCollectorVisitor();
		compilationUnit.accept(methodDeclarationsCollectorVisitor);
		List<MethodDeclaration> allMethodDeclarations = methodDeclarationsCollectorVisitor.getMethodDeclarations();
		MethodDeclaration mainMethodToRemove;
		try {
			mainMethodToRemove = findNonReferencedMainMethod(compilationUnit, allMethodDeclarations).orElse(null);
		} catch (CoreException e) {
			return Optional.empty();
		}
		JUnit3TestMethodsStore jUnitTestMethodStore;
		if (mainMethodToRemove != null) {
			List<MethodDeclaration> methodDeclarationsOutsideMain = allMethodDeclarations
				.stream()
				.filter(methodDeclaration -> !isSurroundedByMethodDeclaration(mainMethodToRemove, methodDeclaration))
				.collect(Collectors.toList());
			jUnitTestMethodStore = new JUnit3TestMethodsStore(methodDeclarationsOutsideMain);
		} else {
			jUnitTestMethodStore = new JUnit3TestMethodsStore(allMethodDeclarations);
		}

		JUnit3AssertionAnalyzer assertionAnalyzer = new JUnit3AssertionAnalyzer(jUnitTestMethodStore,
				classDeclaringMethodReplacement);
		List<JUnit3AssertionAnalysisResult> assertionAnalysisResults = new ArrayList<>();
		MethodInvocationsCollectorVisitor invocationCollectorVisitor = new MethodInvocationsCollectorVisitor();
		compilationUnit.accept(invocationCollectorVisitor);
		for (MethodInvocation methodInvocation : invocationCollectorVisitor.getMethodInvocations()) {
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			if (methodBinding == null) {
				return Optional.empty();
			}
			if (isJUnit3Method(methodBinding)) {
				JUnit3AssertionAnalysisResult assertionAnalysisResult = assertionAnalyzer
					.findAssertionAnalysisResult(methodInvocation, methodBinding)
					.orElse(null);
				if (assertionAnalysisResult != null) {
					assertionAnalysisResults.add(assertionAnalysisResult);
				} else {
					return Optional.empty();
				}
			}
		}
		List<TestMethodAnnotationData> testMethodAnnotationDataList = jUnitTestMethodStore.getJUnit3TestMethods()
			.stream()
			.map(methodDeclaration -> this.createTestMethodAnnotationData(methodDeclaration, migrationConfiguration))
			.collect(Collectors.toList());

		if (mainMethodToRemove != null) {
			return Optional.of(new ReplaceJUnit3TestCasesAnalysisData(testMethodAnnotationDataList,
					assertionAnalysisResults, mainMethodToRemove));
		}
		return Optional
			.of(new ReplaceJUnit3TestCasesAnalysisData(testMethodAnnotationDataList, assertionAnalysisResults));
	}

	private static Optional<MethodDeclaration> findNonReferencedMainMethod(CompilationUnit compilationUnit,
			List<MethodDeclaration> allMethodDeclarations)
			throws CoreException {
		final Optional<MethodDeclaration> optionalMainMethodDeclaration = allMethodDeclarations
			.stream()
			.filter(methodDeclaration -> MethodDeclarationUtils.isJavaApplicationMainMethod(compilationUnit,
					methodDeclaration))
			.findFirst();

		MethodDeclaration mainMethodDeclaration = optionalMainMethodDeclaration.orElse(null);
		if (mainMethodDeclaration == null) {
			return Optional.empty();
		}
		ITypeBinding declaringClass = mainMethodDeclaration.resolveBinding()
			.getDeclaringClass();

		if (findMainMethodMatches(declaringClass).isEmpty()) {
			return optionalMainMethodDeclaration;
		}
		return Optional.empty();
	}

	static boolean isSurroundedByMethodDeclaration(MethodDeclaration surroundingMethodDeclaration, ASTNode node) {
		ASTNode parent = node.getParent();
		while (parent != null) {
			if (parent == surroundingMethodDeclaration) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	private boolean isJUnit3Method(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		String declaringClassQualifiedName = declaringClass
			.getQualifiedName();
		return isJUnit3QualifiedName(declaringClassQualifiedName);
	}

	private TestMethodAnnotationData createTestMethodAnnotationData(MethodDeclaration methodDeclaration,
			Junit3MigrationConfiguration migrationConfiguration) {
		String methodName = methodDeclaration.getName()
			.getIdentifier();
		String annotationQualifiedName;
		if (methodName.equals(JUnit3TestMethodsStore.SET_UP)) {
			annotationQualifiedName = migrationConfiguration.getSetupAnnotationQualifiedName();
		} else if (methodName.equals(JUnit3TestMethodsStore.TEAR_DOWN)) {
			annotationQualifiedName = migrationConfiguration.getTeardownAnnotationQualifiedName();
		} else {
			annotationQualifiedName = migrationConfiguration.getTestAnnotationQualifiedName();
		}
		return new TestMethodAnnotationData(methodDeclaration, annotationQualifiedName);
	}
}
