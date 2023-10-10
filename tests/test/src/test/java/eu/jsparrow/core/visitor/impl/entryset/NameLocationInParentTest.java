package eu.jsparrow.core.visitor.impl.entryset;

import static eu.jsparrow.core.visitor.impl.entryset.TestHelper.createExampleEnumDeclaration;
import static eu.jsparrow.core.visitor.impl.entryset.TestHelper.createExpressionFromString;
import static eu.jsparrow.core.visitor.impl.entryset.TestHelper.createStatementFromString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.jdtunit.JdtUnitFixtureClass;
import eu.jsparrow.jdtunit.JdtUnitFixtureProject;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * TODO: remove {@code ExcludeVariableBindingTest} which is in package
 * {@code eu.jsparrow.rules.common.visitor.helper.test} and move
 * {@link NameLocationInParentTest} to the package corresponding to the package
 * where {@link NameLocationInParent} is declared.
 *
 */
class NameLocationInParentTest {

	private static final String DEFAULT_TYPE_DECLARATION_NAME = "TestCU"; //$NON-NLS-1$

	private static JdtUnitFixtureProject fixtureProject;
	private static JdtUnitFixtureClass defaultFixture;
	private static List<ASTNode> allNodes;
	private static List<SimpleName> allSimpleNames;

	int maxInteger = Integer.MAX_VALUE;

	static final String CODE = "" +
			"	int maxInteger = Integer.MAX_VALUE;" +
			"\n" +
			"	int returnMaxInteger () {\n" +
			"		return this.maxInteger;\n" +
			"	}" +
			"\n" +
			"	int declareAndReturnX() {\n" +
			"		int x = 1;\n" +
			"		return x;\n" +
			"	}" +
			"\n" +
			"	void useObject(Object o) {\n" +
			"	}" +
			"\n" +
			"	void iterateIntArray() {\n" +
			"		int[] xArray = { 1, 2, 3 };\n" +
			"		for (int x : xArray) {\n" +
			"		}\n" +
			"	}" +
			"\n" +
			"	void callXMethod() {\n" +
			"		xMethod();\n" +
			"	}\n" +
			"\n" +
			"	void xMethod() {\n" +
			"	}" +
			"\n" +
			"	enum ExampleEnum{\n" +
			"		ENTRY;\n" +
			"	}" +
			"\n" +
			"	class SuperClass {\n" +
			"		int superField;\n" +
			"		void superMethod() {\n" +
			"		}\n" +
			"	}\n" +
			"\n" +
			"	class SubClass extends SuperClass {\n" +
			"		int getSuperField() {\n" +
			"			return super.superField;\n" +
			"		}\n" +
			"		void callSuperMethod() {\n" +
			"			super.superMethod();\n" +
			"		}\n" +
			"	}";

	@BeforeAll
	public static void setUpClass() throws Exception {
		fixtureProject = new JdtUnitFixtureProject();
		fixtureProject.setUp();
		fixtureProject.setJavaVersion(JavaCore.VERSION_16);
		defaultFixture = fixtureProject.addCompilationUnit(DEFAULT_TYPE_DECLARATION_NAME);
		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, CODE);
		final TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(typeDeclaration, CompilationUnit.class);
		allNodes = new ArrayList<>();
		ASTVisitor allNodesCollectorVisitor = new ASTVisitor() {

			@Override
			public void preVisit(ASTNode node) {
				allNodes.add(node);
			}
		};

		compilationUnit.accept(allNodesCollectorVisitor);

		allSimpleNames = allNodes.stream()
			.filter(SimpleName.class::isInstance)
			.map(SimpleName.class::cast)
			.collect(Collectors.toList());
	}

	private static <T> T findFirst(Class<T> nodeType) {
		return allNodes.stream()
			.filter(nodeType::isInstance)
			.findFirst()
			.map(nodeType::cast)
			.get();
	}

	@AfterAll
	public static void tearDownClass() throws CoreException {
		fixtureProject.clear();
		fixtureProject.tearDown();
	}

	@Test
	void test_MethodInvocationName_noVariableBinding() {
		SimpleName methodInvocationName = allSimpleNames.stream()
			.filter(simpleName -> simpleName.getLocationInParent() == MethodInvocation.NAME_PROPERTY)
			.findFirst()
			.get();
		assertFalse(NameLocationInParent.hasPotentiallyVariableBinding(methodInvocationName));
		IBinding binding = methodInvocationName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);

	}

	@Test
	void test_MethodDeclarationName_noVariableBinding() {
		SimpleName methodInvocationName = allSimpleNames.stream()
			.filter(simpleName -> simpleName.getLocationInParent() == MethodDeclaration.NAME_PROPERTY)
			.findFirst()
			.get();
		assertFalse(NameLocationInParent.hasPotentiallyVariableBinding(methodInvocationName));
		IBinding binding = methodInvocationName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);

	}

	private static Stream<Arguments> locationsInParentExcludingLocalVariableReference() throws Exception {
		return Stream.of(
				Arguments.of(VariableDeclarationFragment.NAME_PROPERTY),
				Arguments.of(SingleVariableDeclaration.NAME_PROPERTY),
				Arguments.of(EnumConstantDeclaration.NAME_PROPERTY),
				Arguments.of(FieldAccess.NAME_PROPERTY),
				Arguments.of(SuperFieldAccess.NAME_PROPERTY),
				Arguments.of(QualifiedName.NAME_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("locationsInParentExcludingLocalVariableReference")
	void testIsPotentialReferenceToLocalVariable_shouldReturnFalse(StructuralPropertyDescriptor locationInParent)
			throws Exception {
		SimpleName variableName = allSimpleNames.stream()
			.filter(simpleName -> simpleName.getLocationInParent() == locationInParent)
			.findFirst()
			.get();
		assertFalse(NameLocationInParent.isPotentialReferenceToLocalVariable(variableName));
		IBinding binding = variableName.resolveBinding();
		assertTrue(binding instanceof IVariableBinding);
	}

	@Test
	void test_LocalVariableDeclarationFragmentName_excludesLocalVariableReference() {
		VariableDeclarationFragment localVariableDeclaratioFragment = (VariableDeclarationFragment) findFirst(
				VariableDeclarationStatement.class)
					.fragments()
					.get(0);
		assertFalse(
				NameLocationInParent.isPotentialReferenceToLocalVariable(localVariableDeclaratioFragment.getName()));
	}

	@Test
	void test_SingleVariableDeclarationInForLoop_excludesLocalVariableReference() {
		SingleVariableDeclaration loopParameter = (SingleVariableDeclaration) findFirst(
				EnhancedForStatement.class)
					.getParameter();
		assertFalse(
				NameLocationInParent.isPotentialReferenceToLocalVariable(loopParameter.getName()));
	}

	@Test
	void testIsPotentialReferenceToLocalVariable_shouldReturnTrue()
			throws Exception {
		SimpleName simpleName = createExpressionFromString("x", SimpleName.class);
		assertTrue(NameLocationInParent.isPotentialReferenceToLocalVariable(simpleName));
	}

}
