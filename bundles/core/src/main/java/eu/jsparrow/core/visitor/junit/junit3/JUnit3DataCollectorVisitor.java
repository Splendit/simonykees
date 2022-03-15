package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.core.visitor.junit.junit3.UnexpectedJunit3References.isJUnit3Name;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.core.visitor.junit.jupiter.common.MethodDeclarationsCollectorVisitor;
import eu.jsparrow.core.visitor.utils.MethodDeclarationUtils;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Visitor collecting all declarations which affect the migration of JUnit 3
 * assertions to either JUnit 4 or JUnit Jupiter, for example:
 * <ul>
 * <li>{@link ImportDeclaration}-nodes which can be removed.</li>
 * <li>{@link TypeDeclaration}-nodes which represent JUnit3 test cases.</li>
 * <li>{@link MethodDeclaration}-nodes which represent JUnit3 test methods or
 * represent the {@code setup} method or the {@code tearDown} method..</li>
 * </ul>
 * 
 * This visitor may find out that transformation cannot be carried out due to
 * declarations which prohibit transformation.
 *
 * @since 4.1.0
 * 
 */
public class JUnit3DataCollectorVisitor extends ASTVisitor {
	static final String JUNIT_FRAMEWORK_TEST_CASE = "junit.framework.TestCase"; //$NON-NLS-1$
	private static final String JUNIT_FRAMEWORK_TEST_RESULT = "junit.framework.TestResult"; //$NON-NLS-1$
	private static final String JAVA_LANG_STRING = "java.lang.String"; //$NON-NLS-1$
	private static final String JAVA_LANG_OVERRIDE = "java.lang.Override"; //$NON-NLS-1$
	private static final String VOID = "void"; //$NON-NLS-1$
	static final String SET_UP = "setUp"; //$NON-NLS-1$
	static final String TEAR_DOWN = "tearDown"; //$NON-NLS-1$
	static final String TEST = "test"; //$NON-NLS-1$

	private final CompilationUnit compilationUnit;
	private final Junit3MigrationConfiguration migrationConfiguration;
	private final String classDeclaringMethodReplacement;
	private final List<ImportDeclaration> importDeclarationsToRemove = new ArrayList<>();
	private final List<TypeDeclaration> jUnit3TestCaseDeclarations = new ArrayList<>();
	private final List<SimpleType> jUnit3TestCaseSuperTypesToRemove = new ArrayList<>();
	private final List<MethodDeclaration> jUnit3TestMethodDeclarations = new ArrayList<>();
	private final List<TestMethodAnnotationData> testMethodAnnotationDataList = new ArrayList<>();
	private final List<Annotation> overrideAnnotationsToRemove = new ArrayList<>();
	private final List<JUnit3AssertionAnalysisResult> jUnit3AssertionAnalysisResults = new ArrayList<>();
	private final List<ExpressionStatement> superMethodInvocationsToRemove = new ArrayList<>();
	private final Map<ExpressionStatement, TypeLiteral> runInvocationToTypeLiteralMap = new HashMap<>();
	private MethodDeclaration mainMethodToRemove;
	private boolean transformationPossible = true;

	JUnit3DataCollectorVisitor(Junit3MigrationConfiguration migrationConfiguration, CompilationUnit compilationUnit) {
		this.migrationConfiguration = migrationConfiguration;
		this.classDeclaringMethodReplacement = migrationConfiguration.getAssertionClassQualifiedName();
		this.compilationUnit = compilationUnit;
		MethodDeclarationsCollectorVisitor methodDeclarationsCollectorVisitor = new MethodDeclarationsCollectorVisitor();
		compilationUnit.accept(methodDeclarationsCollectorVisitor);
		List<MethodDeclaration> allMethodDeclarations = methodDeclarationsCollectorVisitor.getMethodDeclarations();
		mainMethodToRemove = allMethodDeclarations
			.stream()
			.filter(methodDeclaration -> MethodDeclarationUtils.isJavaApplicationMainMethod(compilationUnit,
					methodDeclaration))
			.findFirst()
			.orElse(null);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		String packageName = node.resolveBinding()
			.getName();
		transformationPossible = !isJUnit3Name(packageName);
		return false;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		String fullyQualifiedName = node.getName()
			.getFullyQualifiedName();
		if (isJUnit3Name(fullyQualifiedName)) {
			importDeclarationsToRemove.add(node);
			return false;
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		SimpleType testCaseAsSuperType = findTestCaseAsSuperType(node).orElse(null);
		if (testCaseAsSuperType != null && isValidJUnit3TestCaseSubclass(node)) {
			jUnit3TestCaseDeclarations.add(node);
			jUnit3TestCaseSuperTypesToRemove.add(testCaseAsSuperType);
		}
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (node.getLocationInParent() == TypeDeclaration.BODY_DECLARATIONS_PROPERTY
				&& jUnit3TestCaseDeclarations.contains(node.getParent())) {
			if (isOverridingJUnitFrameworkTestCaseMethod(node)) {
				ASTNodeUtil
					.convertToTypedList(node.modifiers(), IExtendedModifier.class)
					.stream()
					.filter(IExtendedModifier::isAnnotation)
					.map(Annotation.class::cast)
					.filter(annotation -> ClassRelationUtil.isContentOfType(annotation.resolveTypeBinding(),
							JAVA_LANG_OVERRIDE))
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
		if (mainMethodToRemove != null &&
				ASTNodeUtil.getSpecificAncestor(node, MethodDeclaration.class) == mainMethodToRemove) {
			ExpressionStatement parentExpressionStatement = findParentExpressionStatement(node).orElse(null);
			if (parentExpressionStatement != null) {
				TypeLiteral runTestInvocationTypeLiteralArgument = findRunTestInvocationTypeLiteralArgument(node)
					.orElse(null);
				if (runTestInvocationTypeLiteralArgument != null) {
					runInvocationToTypeLiteralMap.put(parentExpressionStatement, runTestInvocationTypeLiteralArgument);
					return false;
				}
			}
		}
		JUnit3AssertionAnalyzer assertionAnalyzer = new JUnit3AssertionAnalyzer();
		transformationPossible = assertionAnalyzer.analyzeMethodInvocation(classDeclaringMethodReplacement,
				compilationUnit, node);
		if (transformationPossible) {
			assertionAnalyzer.getAssertionAnalysisResult()
				.ifPresent(jUnit3AssertionAnalysisResults::add);
		}
		return transformationPossible;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding == null) {
			transformationPossible = false;
			return false;
		}

		String methodName = methodBinding.getName();
		if (methodName.equals(SET_UP) || methodName.equals(TEAR_DOWN)) {
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			if (ClassRelationUtil.isContentOfType(declaringClass, JUNIT_FRAMEWORK_TEST_CASE)
					&& node.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				ExpressionStatement expressionStatement = (ExpressionStatement) node.getParent();
				if (expressionStatement.getLocationInParent() == Block.STATEMENTS_PROPERTY) {
					superMethodInvocationsToRemove.add(expressionStatement);
					return false;
				}
			}
		}
		transformationPossible = UnexpectedJunit3References.analyzeMethodBinding(compilationUnit, methodBinding);
		return transformationPossible;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		transformationPossible = UnexpectedJunit3References.analyzeMethodBinding(compilationUnit,
				node.resolveConstructorBinding());
		return transformationPossible;
	}

	@Override
	public boolean visit(QualifiedName node) {
		transformationPossible = analyzeName(node);
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		transformationPossible = ASTNodeUtil.isLabel(node) || analyzeName(node);
		return false;
	}

	private Optional<SimpleType> findTestCaseAsSuperType(TypeDeclaration node) {

		Type superclassType = node.getSuperclassType();
		if (superclassType != null && superclassType.getNodeType() == ASTNode.SIMPLE_TYPE) {
			SimpleType simpleSuperClassType = (SimpleType) superclassType;
			ITypeBinding typeBinding = simpleSuperClassType.resolveBinding();
			String qualifiedName = typeBinding.getQualifiedName();
			if (qualifiedName.equals(JUNIT_FRAMEWORK_TEST_CASE)) {
				return Optional.of(simpleSuperClassType);
			}
		}
		return Optional.empty();
	}

	private boolean isValidJUnit3TestCaseSubclass(TypeDeclaration testCaseSubclassDeclaration) {
		int modifiers = testCaseSubclassDeclaration.getModifiers();
		if (Modifier.isAbstract(modifiers)) {
			return false;
		}
		if (!Modifier.isPublic(modifiers)) {
			return false;
		}
		if (Modifier.isStatic(modifiers)) {
			return true;
		}
		return MainTopLevelJavaClass.isMainTopLevelClass(testCaseSubclassDeclaration);
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
		if (methodName.equals(SET_UP) || methodName.equals(TEAR_DOWN)) {
			return true;
		}

		if (!methodName.startsWith(TEST)) {
			return false;
		}

		int modifiers = methodBinding.getModifiers();
		if (!Modifier.isPublic(modifiers)) {
			return false;
		}

		String returnTypeQualifiedName = methodBinding.getReturnType()
			.getQualifiedName();

		return returnTypeQualifiedName.equals(VOID);
	}

	private static TestMethodAnnotationData createTestMethodAnnotationData(MethodDeclaration methodDeclaration,
			Junit3MigrationConfiguration migrationConfiguration) {
		String methodName = methodDeclaration.getName()
			.getIdentifier();
		String annotationQualifiedName;
		if (methodName.equals(SET_UP)) {
			annotationQualifiedName = migrationConfiguration.getSetUpAnnotationQualifiedName();
		} else if (methodName.equals(TEAR_DOWN)) {
			annotationQualifiedName = migrationConfiguration.getTearDownAnnotationQualifiedName();
		} else {
			annotationQualifiedName = migrationConfiguration.getTestAnnotationQualifiedName();
		}
		return new TestMethodAnnotationData(methodDeclaration, annotationQualifiedName);
	}

	private boolean analyzeName(Name name) {
		if (name.getLocationInParent() == PackageDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == TypeDeclaration.NAME_PROPERTY
				|| (name.getLocationInParent() == SimpleType.NAME_PROPERTY
						&& jUnit3TestCaseSuperTypesToRemove.contains(name.getParent()))
				|| name.getLocationInParent() == MethodDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == MethodInvocation.NAME_PROPERTY
				|| name.getLocationInParent() == MethodInvocation.EXPRESSION_PROPERTY) {
			return true;
		}
		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return false;
		}
		return UnexpectedJunit3References.analyzeNameBinding(compilationUnit, binding);
	}

	private static Optional<ExpressionStatement> findParentExpressionStatement(MethodInvocation methodInvocation) {

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) methodInvocation.getParent();
		if (expressionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}
		return Optional.of(expressionStatement);
	}

	private static Optional<TypeLiteral> findRunTestInvocationTypeLiteralArgument(MethodInvocation methodInvocation) {

		if (!methodInvocation.getName()
			.getIdentifier()
			.equals("run")) { //$NON-NLS-1$
			return Optional.empty();
		}

		List<TypeLiteral> listWithExpectedTypeLiteral = ASTNodeUtil.returnTypedList(methodInvocation.arguments(),
				TypeLiteral.class);

		if (listWithExpectedTypeLiteral.size() != 1) {
			return Optional.empty();
		}
		TypeLiteral typeLiteral = listWithExpectedTypeLiteral.get(0);

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Optional.empty();
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (ClassRelationUtil.isContentOfType(declaringClass, "junit.textui.TestRunner")) { //$NON-NLS-1$
			return Optional.of(typeLiteral);
		}
		return Optional.empty();
	}

	public List<ImportDeclaration> getImportDeclarationsToRemove() {
		return importDeclarationsToRemove;
	}

	public List<TypeDeclaration> getJUnit3TestCaseDeclarations() {
		return jUnit3TestCaseDeclarations;
	}

	public List<JUnit3AssertionAnalysisResult> getJUnit3AssertionAnalysisResults() {
		return jUnit3AssertionAnalysisResults;
	}

	public List<SimpleType> getJUnit3TestCaseSuperTypesToRemove() {
		return jUnit3TestCaseSuperTypesToRemove;
	}

	public List<Annotation> getOverrideAnnotationsToRemove() {
		return overrideAnnotationsToRemove;
	}

	public List<ExpressionStatement> getSuperMethodInvocationsToRemove() {
		return superMethodInvocationsToRemove;
	}

	public List<TestMethodAnnotationData> getTestMethodAnnotationDataList() {
		return testMethodAnnotationDataList;
	}

	public Optional<MethodDeclaration> getMainMethodToRemove() {
		return Optional.ofNullable(mainMethodToRemove);
	}

	public Map<ExpressionStatement, TypeLiteral> getRunInvocationToTypeLiteralMap() {
		return runInvocationToTypeLiteralMap;
	}

	/**
	 * 
	 * @return true if transformation can be carried out for the given
	 *         {@link CompilationUnit}, otherwise false.
	 */
	public boolean isTransformationPossible() {
		if (importDeclarationsToRemove.isEmpty() &&
				jUnit3TestCaseDeclarations.isEmpty() &&
				jUnit3TestMethodDeclarations.isEmpty() &&
				testMethodAnnotationDataList.isEmpty() &&
				overrideAnnotationsToRemove.isEmpty() &&
				jUnit3AssertionAnalysisResults.isEmpty() &&
				superMethodInvocationsToRemove.isEmpty() &&
				runInvocationToTypeLiteralMap.isEmpty()) {
			return false;
		}
		return transformationPossible;
	}
}
