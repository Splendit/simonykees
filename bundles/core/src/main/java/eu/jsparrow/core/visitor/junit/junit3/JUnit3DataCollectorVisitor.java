package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.utils.MainMethodMatches.findMainMethodMatches;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Visitor collecting all type declarations, method declarations and method
 * invocations which will have to be analyzed. Additionally, this visitor
 * determines whether there is a main method which can be removed.
 *
 */
public class JUnit3DataCollectorVisitor extends ASTVisitor {
	private static final String JUNIT_FRAMEWORK_TEST_CASE = "junit.framework.TestCase"; //$NON-NLS-1$
	private static final String JUNIT_FRAMEWORK_TEST_RESULT = "junit.framework.TestResult"; //$NON-NLS-1$
	private static final String JAVA_LANG_STRING = "java.lang.String"; //$NON-NLS-1$
	private static final String JAVA_LANG_OVERRIDE = "java.lang.Override"; //$NON-NLS-1$
	static final String SET_UP = "setUp"; //$NON-NLS-1$
	static final String TEAR_DOWN = "tearDown"; //$NON-NLS-1$
	static final String TEST = "test"; //$NON-NLS-1$

	private final Junit3MigrationConfiguration migrationConfiguration;
	private final List<ImportDeclaration> importDeclarationsToRemove = new ArrayList<>();
	private final List<TypeDeclaration> jUnit3TestCaseDeclarations = new ArrayList<>();
	private final List<SimpleType> jUnit3TestCaseSuperTypesToRemove = new ArrayList<>();
	private final List<MethodDeclaration> jUnit3TestMethodDeclarations = new ArrayList<>();
	private final List<TestMethodAnnotationData> testMethodAnnotationDataList = new ArrayList<>();
	private final List<Annotation> overrideAnnotationsToRemove = new ArrayList<>();
	private final List<MethodInvocation> methodInvocationsToAnalyze = new ArrayList<>();
	private MethodDeclaration mainMethodToRemove;
	private boolean transformationPossible = true;

	JUnit3DataCollectorVisitor(Junit3MigrationConfiguration migrationConfiguration) {
		this.migrationConfiguration = migrationConfiguration;
	}

	static Optional<MethodDeclaration> findMainMethodToRemove(CompilationUnit compilationUnit) {
		MethodDeclarationsCollectorVisitor methodDeclarationsCollectorVisitor = new MethodDeclarationsCollectorVisitor();
		compilationUnit.accept(methodDeclarationsCollectorVisitor);
		List<MethodDeclaration> allMethodDeclarations = methodDeclarationsCollectorVisitor.getMethodDeclarations();
		MethodDeclaration mainMethodDeclaration = allMethodDeclarations
			.stream()
			.filter(methodDeclaration -> MethodDeclarationUtils.isJavaApplicationMainMethod(compilationUnit,
					methodDeclaration))
			.findFirst()
			.orElse(null);

		if (mainMethodDeclaration != null) {
			ITypeBinding declaringClass = mainMethodDeclaration.resolveBinding()
				.getDeclaringClass();
			try {
				if (findMainMethodMatches(declaringClass).isEmpty()) {
					return Optional.of(mainMethodDeclaration);
				}
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		mainMethodToRemove = findMainMethodToRemove(node).orElse(null);
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		String packageName = node.resolveBinding()
			.getName();
		transformationPossible = !UnexpectedJunit3References.isUnexpectedJUnitQualifiedName(packageName);
		return false;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		String fullyQualifiedName = node.getName()
			.getFullyQualifiedName();
		if (fullyQualifiedName.startsWith("junit.")) { //$NON-NLS-1$
			importDeclarationsToRemove.add(node);
		} else if (UnexpectedJunit3References.isUnexpectedJUnitQualifiedName(fullyQualifiedName)) {
			transformationPossible = false;
		}
		return false;
	}

	@Override
	public boolean visit(SimpleType node) {
		if (node.getLocationInParent() == TypeDeclaration.SUPERCLASS_TYPE_PROPERTY) {
			ITypeBinding typeBinding = node.resolveBinding();
			if (typeBinding != null && typeBinding.getQualifiedName()
				.equals(JUNIT_FRAMEWORK_TEST_CASE)) {
				TypeDeclaration testCaseTypeDeclaration = (TypeDeclaration) node.getParent();
				if (!testCaseTypeDeclaration.isLocalTypeDeclaration()) {
					jUnit3TestCaseDeclarations.add(testCaseTypeDeclaration);
					jUnit3TestCaseSuperTypesToRemove.add(node);
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (mainMethodToRemove != null && mainMethodToRemove == node) {
			return false;
		}
		if (node.getLocationInParent() == TypeDeclaration.BODY_DECLARATIONS_PROPERTY
				&& jUnit3TestCaseDeclarations.contains(node.getParent())) {
			if (isOverridingJUnitFrameworkTestCaseMethod(node)) {
				ASTNodeUtil
					.convertToTypedList(node.modifiers(), IExtendedModifier.class)
					.stream()
					.filter(IExtendedModifier::isAnnotation)
					.map(Annotation.class::cast)
					.filter(annotation -> annotation.resolveTypeBinding()
						.getQualifiedName()
						.equals(JAVA_LANG_OVERRIDE))
					.findFirst()
					.ifPresent(overrideAnnotationsToRemove::add);
			}
			if (isTestMethodDeclaration(node)) {
				jUnit3TestMethodDeclarations.add(node);
				testMethodAnnotationDataList
					.add(createTestMethodAnnotationData(node, migrationConfiguration));
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		methodInvocationsToAnalyze.add(node);
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		transformationPossible = analyzeName(node);
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		transformationPossible = analyzeName(node);
		return false;
	}

	private boolean analyzeName(Name name) {
		if (name.getLocationInParent() == PackageDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == ImportDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == TypeDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == MethodDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == MethodInvocation.NAME_PROPERTY
				|| name.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY
				|| name.getLocationInParent() == LabeledStatement.LABEL_PROPERTY
				|| name.getLocationInParent() == ContinueStatement.LABEL_PROPERTY
				|| name.getLocationInParent() == BreakStatement.LABEL_PROPERTY) {
			return true;
		}

		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return false;
		}

		ITypeBinding typeBinding = null;
		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			typeBinding = methodBinding.getDeclaringClass();
		}

		if (binding.getKind() == IBinding.TYPE) {
			typeBinding = (ITypeBinding) binding;
		}

		if (binding.getKind() == IBinding.ANNOTATION) {
			IAnnotationBinding annotationBinding = (IAnnotationBinding) binding;
			typeBinding = annotationBinding.getAnnotationType();
		}

		if (binding.getKind() == IBinding.MEMBER_VALUE_PAIR) {
			IMemberValuePairBinding memberValuePairBinding = (IMemberValuePairBinding) binding;
			IMethodBinding methodBinding = memberValuePairBinding.getMethodBinding();
			typeBinding = methodBinding.getDeclaringClass();
		}

		if (typeBinding != null) {
			return !UnexpectedJunit3References.hasUnexpectedJUnitReference(typeBinding);
		}

		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			ITypeBinding variableTypeBinding = variableBinding.getVariableDeclaration()
				.getType();
			if (UnexpectedJunit3References.hasUnexpectedJUnitReference(variableTypeBinding)) {
				return false;
			}
			if (variableBinding.isField()) {
				ITypeBinding fieldDeclaringClass = variableBinding.getDeclaringClass();
				if (fieldDeclaringClass != null
						&& UnexpectedJunit3References.hasUnexpectedJUnitReference(fieldDeclaringClass)) {
					return false;
				}
			}
			return true;
		}
		return false;
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

	private static boolean isTestMethodDeclaration(MethodDeclaration junitTestCaseMethodDeclaration) {

		IMethodBinding methodBinding = junitTestCaseMethodDeclaration.resolveBinding();
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

	public List<ImportDeclaration> getImportDeclarationsToRemove() {
		return importDeclarationsToRemove;
	}

	public List<MethodInvocation> getMethodInvocationsToAnalyze() {
		return methodInvocationsToAnalyze;
	}

	public boolean isTransformationPossible() {
		return transformationPossible;
	}

	public List<TypeDeclaration> getJUnit3TestCaseDeclarations() {
		return jUnit3TestCaseDeclarations;
	}

	public List<SimpleType> getJUnit3TestCaseSuperTypesToRemove() {
		return jUnit3TestCaseSuperTypesToRemove;
	}

	public List<Annotation> getOverrideAnnotationsToRemove() {
		return overrideAnnotationsToRemove;
	}

	public List<MethodDeclaration> getJUnit3TestMethodDeclarations() {
		return jUnit3TestMethodDeclarations;
	}

	public List<TestMethodAnnotationData> getTestMethodAnnotationDataList() {
		return testMethodAnnotationDataList;
	}

	public Optional<MethodDeclaration> getMainMethodToRemove() {
		return Optional.ofNullable(mainMethodToRemove);
	}
}
