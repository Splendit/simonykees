package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class JUnit3TestCaseDeclarationsAnalyzer {
	private static final String JUNIT_FRAMEWORK_TEST_RESULT = "junit.framework.TestResult"; //$NON-NLS-1$
	private static final String JAVA_LANG_STRING = "java.lang.String"; //$NON-NLS-1$
	private static final String JAVA_LANG_OVERRIDE = "java.lang.Override"; //$NON-NLS-1$
	static final String SET_UP = "setUp"; //$NON-NLS-1$
	static final String TEAR_DOWN = "tearDown"; //$NON-NLS-1$
	static final String TEST = "test"; //$NON-NLS-1$

	private static final String JUNIT_FRAMEWORK_TEST_CASE = "junit.framework.TestCase"; //$NON-NLS-1$

	private MethodDeclaration mainMethodToRemove;
	private final List<TypeDeclaration> jUnit3TestCaseDeclarations = new ArrayList<>();
	private final List<SimpleType> jUnit3TestCaseSuperTypesToRemove = new ArrayList<>();
	private final List<MethodDeclaration> jUnit3TestMethodDeclarations = new ArrayList<>();
	private final List<TestMethodAnnotationData> testMethodAnnotationDataList = new ArrayList<>();
	private final List<Annotation> overrideAnnotationsToRemove = new ArrayList<>();

	boolean collectTestCaseDeclarationAnalysisData(JUnit3DataCollectorVisitor junit3DataCollectorVisitor,
			Junit3MigrationConfiguration migrationConfiguration) {
		
		mainMethodToRemove = junit3DataCollectorVisitor.getMainMethodToRemove().orElse(null);
		
		List<TypeDeclaration> typeDeclarationsToAnalyze = junit3DataCollectorVisitor.getTypeDeclarationsToAnalyze();
		for (TypeDeclaration typeDeclaration : typeDeclarationsToAnalyze) {
			if (isWellFormedTestCaseTypeDeclaration(typeDeclaration)) {
				jUnit3TestCaseDeclarations.add(typeDeclaration);
			} else if (UnexpectedJunit3References.hasUnexpectedJUnitReference(typeDeclaration.resolveBinding())) {
				return false;
			}
		}

		List<SimpleType> superClassSimpleTypesToAnalyze = junit3DataCollectorVisitor.getSuperClassSimpleTypesToAnalyze();
		for (SimpleType superClassSimpleType : superClassSimpleTypesToAnalyze) {
			if (jUnit3TestCaseDeclarations.contains(superClassSimpleType.getParent())) {
				jUnit3TestCaseSuperTypesToRemove.add(superClassSimpleType);
			} else if (UnexpectedJunit3References.hasUnexpectedJUnitReference(superClassSimpleType.resolveBinding())) {
				return false;
			}
		}
		List<MethodDeclaration> methodDeclarationsToAnalyze = junit3DataCollectorVisitor
			.getMethodDeclarationsToAnalyze();

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

	private static boolean isWellFormedTestCaseTypeDeclaration(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.isLocalTypeDeclaration()) {
			return false;
		}
		Type superclassType = typeDeclaration.getSuperclassType();
		if (superclassType == null) {
			return false;
		}
		if (superclassType.getNodeType() != ASTNode.SIMPLE_TYPE) {
			return false;
		}
		String superClassQualifiedName = superclassType.resolveBinding()
			.getQualifiedName();

		if (!superClassQualifiedName.equals(JUNIT_FRAMEWORK_TEST_CASE)) {
			return false;
		}

		ITypeBinding[] interfacesToAnalyze = typeDeclaration.resolveBinding()
			.getInterfaces();
		for (ITypeBinding implementedInterface : interfacesToAnalyze) {
			if (UnexpectedJunit3References.hasUnexpectedJUnitReference(implementedInterface)) {
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

	public List<TypeDeclaration> getJUnit3TestCaseDeclarations() {
		return jUnit3TestCaseDeclarations;
	}

	public List<SimpleType> getJUnit3TestCaseSuperTypesToRemove() {
		return jUnit3TestCaseSuperTypesToRemove;
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
	
	public Optional<MethodDeclaration> getMainMethodToRemove() {
		return Optional.ofNullable(mainMethodToRemove);
	}
}
