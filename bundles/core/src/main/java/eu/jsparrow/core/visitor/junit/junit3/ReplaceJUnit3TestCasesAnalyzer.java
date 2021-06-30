package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class ReplaceJUnit3TestCasesAnalyzer {

	static final String SET_UP = "setUp"; //$NON-NLS-1$
	static final String TEAR_DOWN = "tearDown"; //$NON-NLS-1$
	static final String TEST = "test"; //$NON-NLS-1$

	public Optional<ReplaceJUnit3TestCasesAnalysisData> analyzeCompilationUnit(CompilationUnit compilationUnit,
			Junit3MigrationConfiguration migrationConfiguration, String classDeclaringMethodReplacement) {

		JUnit3DataCollectorVisitor junit3DataCollectorVisitor = new JUnit3DataCollectorVisitor();
		compilationUnit.accept(junit3DataCollectorVisitor);
		JUnit3TestCaseAnalyzer jUnit3TestCaseAnalyzer = new JUnit3TestCaseAnalyzer(junit3DataCollectorVisitor);
		if(!jUnit3TestCaseAnalyzer.collectTestCaseDeclarationData()) {
			return Optional.empty();
		}
		JUnit3TestMethodsStore jUnitTestMethodStore = new JUnit3TestMethodsStore(junit3DataCollectorVisitor, jUnit3TestCaseAnalyzer);

		JUnit3AssertionAnalyzer assertionAnalyzer = new JUnit3AssertionAnalyzer(jUnitTestMethodStore,
				classDeclaringMethodReplacement);
		List<JUnit3AssertionAnalysisResult> assertionAnalysisResults = new ArrayList<>();

		List<MethodInvocation> methodInvocationsToAnalyze = junit3DataCollectorVisitor.getMethodInvocationsToAnalyze();
		for (MethodInvocation methodInvocation : methodInvocationsToAnalyze) {
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

		MethodDeclaration mainMethodToRemove = junit3DataCollectorVisitor.getMainMethodToRemove()
			.orElse(null);
		if (mainMethodToRemove != null) {
			return Optional.of(new ReplaceJUnit3TestCasesAnalysisData(testMethodAnnotationDataList,
					assertionAnalysisResults, mainMethodToRemove));
		}
		return Optional
			.of(new ReplaceJUnit3TestCasesAnalysisData(testMethodAnnotationDataList, assertionAnalysisResults));
	}

	private boolean isJUnit3Method(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		String declaringClassQualifiedName = declaringClass
			.getQualifiedName();
		return isJUnitName(declaringClassQualifiedName);
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
