package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class JUnit3TestMethodDeclarationsAnalyzer {
	private static final String JUNIT_FRAMEWORK_TEST_RESULT = "junit.framework.TestResult"; //$NON-NLS-1$
	private static final String JAVA_LANG_STRING = "java.lang.String"; //$NON-NLS-1$
	private static final String JAVA_LANG_OVERRIDE = "java.lang.Override"; //$NON-NLS-1$
	static final String SET_UP = "setUp"; //$NON-NLS-1$
	static final String TEAR_DOWN = "tearDown"; //$NON-NLS-1$
	static final String TEST = "test"; //$NON-NLS-1$

	private final List<MethodDeclaration> jUnit3TestMethodDeclarations = new ArrayList<>();
	private final List<TestMethodAnnotationData> testMethodAnnotationDataList = new ArrayList<>();
	private final List<Annotation> overrideAnnotationsToRemove = new ArrayList<>();

	boolean collectMethodDeclarationAnalysisData(JUnit3DataCollectorVisitor junit3DataCollectorVisitor,
			JUnit3TestCaseDeclarationsAnalyzer replaceJUnit3TestCasesAnalyzer,
			Junit3MigrationConfiguration migrationConfiguration) {
		List<MethodDeclaration> methodDeclarationsToAnalyze = junit3DataCollectorVisitor
			.getMethodDeclarationsToAnalyze();
		List<TypeDeclaration> jUnit3TestCaseDeclarations = replaceJUnit3TestCasesAnalyzer
			.getJUnit3TestCaseDeclarations();

		List<MethodDeclaration> allJUnit3TestCaseMethodDeclarations = methodDeclarationsToAnalyze
			.stream()
			.filter(methodDeclaration -> jUnit3TestCaseDeclarations.contains(methodDeclaration.getParent()))
			.collect(Collectors.toList());

		for (MethodDeclaration methodDeclaration : allJUnit3TestCaseMethodDeclarations) {
			if (isOverridingJUnitFrameworkTestCaseMethod(methodDeclaration)) {
				ASTNodeUtil
					.convertToTypedList(methodDeclaration.modifiers(), IExtendedModifier.class)
					.stream()
					.filter(IExtendedModifier::isAnnotation)
					.map(Annotation.class::cast)
					.filter(annotation -> annotation.resolveTypeBinding()
						.getQualifiedName()
						.equals(JAVA_LANG_OVERRIDE))
					.findFirst()
					.ifPresent(overrideAnnotationsToRemove::add);
			}
		}

		for (MethodDeclaration methodDeclaration : methodDeclarationsToAnalyze) {
			if (isTestMethodDeclaration(jUnit3TestCaseDeclarations, methodDeclaration)) {
				jUnit3TestMethodDeclarations.add(methodDeclaration);
				testMethodAnnotationDataList
					.add(createTestMethodAnnotationData(methodDeclaration, migrationConfiguration));
			} else if (isUnexpectedMethodDeclaration(jUnit3TestCaseDeclarations, methodDeclaration)) {
				return false;
			}
		}
		return true;
	}

	private static boolean isOverridingJUnitFrameworkTestCaseMethod(MethodDeclaration jUnitTestCaseMethodDeclaration) {
		String methodDeclarationIdentifier = jUnitTestCaseMethodDeclaration.getName()
			.getIdentifier();
		List<SingleVariableDeclaration> parameters = ASTNodeUtil
			.convertToTypedList(jUnitTestCaseMethodDeclaration.parameters(), SingleVariableDeclaration.class);
		if (parameters.isEmpty()) {

			return methodDeclarationIdentifier.equals(SET_UP) ||
					methodDeclarationIdentifier.equals(TEAR_DOWN) ||
					methodDeclarationIdentifier.equals("countTestCases") || //$NON-NLS-1$
					methodDeclarationIdentifier.equals("createResult") || //$NON-NLS-1$
					methodDeclarationIdentifier.equals("getName") || //$NON-NLS-1$
					methodDeclarationIdentifier.equals("run") || //$NON-NLS-1$
					methodDeclarationIdentifier.equals("runBare") || //$NON-NLS-1$
					methodDeclarationIdentifier.equals("runTest"); //$NON-NLS-1$
		}

		if (methodDeclarationIdentifier.equals("setName") //$NON-NLS-1$
				&& parameters.size() == 1) {
			ITypeBinding parameterType = parameters.get(0)
				.resolveBinding()
				.getType();

			return ClassRelationUtil.isContentOfType(parameterType, JAVA_LANG_STRING);
		}

		if (methodDeclarationIdentifier.equals("run") //$NON-NLS-1$
				&& parameters.size() == 1) {
			ITypeBinding parameterType = parameters.get(0)
				.resolveBinding()
				.getType();
			return ClassRelationUtil.isContentOfType(parameterType, JUNIT_FRAMEWORK_TEST_RESULT) ||
					ClassRelationUtil.isInheritingContentOfTypes(parameterType,
							Collections.singletonList(JUNIT_FRAMEWORK_TEST_RESULT));
		}

		return false;
	}

	private static boolean isTestMethodDeclaration(List<TypeDeclaration> jUnit3TestCaseDeclarations,
			MethodDeclaration methodDeclaration) {
		if (!jUnit3TestCaseDeclarations.contains(methodDeclaration.getParent())) {
			return false;
		}

		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		if (methodBinding.getParameterTypes().length != 0) {
			return false;
		}

		String methodName = methodBinding.getName();
		return methodName.startsWith(TEST) || methodName.equals(SET_UP) || methodName.equals(TEAR_DOWN);
	}

	private static TestMethodAnnotationData createTestMethodAnnotationData(MethodDeclaration methodDeclaration,
			Junit3MigrationConfiguration migrationConfiguration) {
		String methodName = methodDeclaration.getName()
			.getIdentifier();
		String annotationQualifiedName;
		if (methodName.equals(SET_UP)) {
			annotationQualifiedName = migrationConfiguration.getSetupAnnotationQualifiedName();
		} else if (methodName.equals(TEAR_DOWN)) {
			annotationQualifiedName = migrationConfiguration.getTeardownAnnotationQualifiedName();
		} else {
			annotationQualifiedName = migrationConfiguration.getTestAnnotationQualifiedName();
		}
		return new TestMethodAnnotationData(methodDeclaration, annotationQualifiedName);
	}

	private static boolean isUnexpectedMethodDeclaration(List<TypeDeclaration> jUnit3TestCaseDeclarations,
			MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();

		if (!jUnit3TestCaseDeclarations.contains(methodDeclaration.getParent())) {
			return UnexpectedJunit3References.hasUnexpectedJUnitReference(methodBinding);
		}

		return false;
	}

	public List<MethodDeclaration> getJUnit3TestMethodDeclarations() {
		return jUnit3TestMethodDeclarations;
	}

	public List<TestMethodAnnotationData> getTestMethodAnnotationDataList() {
		return testMethodAnnotationDataList;
	}

	public List<Annotation> getOverrideAnnotationsToRemove() {
		return overrideAnnotationsToRemove;
	}
}
