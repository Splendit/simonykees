package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.JdtUnitFixtureClass;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings("nls")
public class HideDefaultConstructorInUtilityClassesASTVisitorTest extends UsesJDTUnitFixture {

	private static final String DEFAULT_TYPE_DECLARATION_NAME = "TestCU";

	private HideDefaultConstructorInUtilityClassesASTVisitor visitor;
	private JdtUnitFixtureClass defaultFixture;

	@BeforeEach
	public void setUp() throws Exception {
		defaultFixture = fixtureProject.addCompilationUnit("TestCU");

		visitor = new HideDefaultConstructorInUtilityClassesASTVisitor(
				new IJavaElement[] { fixtureProject.getJavaProject() });
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void test_allCriteriaSatisfied_shouldTransform() throws Exception {
		String actual = "public static void sayHallo() {" + "    System.out.println(\"Hallo\");" + "}";
		String expected = "private TestCU() {" + "}" + "" + "public static void sayHallo() {"
				+ "    System.out.println(\"Hallo\");" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actual);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, expected),
				defaultFixture.getTypeDeclaration());
	}

	@Test
	@Disabled
	/*
	 * TODO: search engine works for normal eclipse instances but for some
	 * reason not with the fixtureproject. This has to be investigated and fixed
	 */
	public void test_defaultConstructorInvokedInOtherClass_shouldNotTransform() throws Exception {
		String actualAndExpected = "public static void test() {" + "}";

		JdtUnitFixtureClass testClass2 = fixtureProject.addCompilationUnit("Test2");
		testClass2.addMethod("testMethodWithDefaultConstructorInvocation",
				DEFAULT_TYPE_DECLARATION_NAME + " t = new " + DEFAULT_TYPE_DECLARATION_NAME + "();");

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected),
				defaultFixture.getTypeDeclaration());
	}

	@Test
	public void test_nonStaticMethodPresent_shoudldNotTransform() throws Exception {
		String acutalAndExpected = "public static void test() {" + "	System.out.println(\"test\");" + "}" + ""
				+ "public String nonStaticMethod() {" + "	return \"non-static method\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, acutalAndExpected);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, acutalAndExpected),
				defaultFixture.getTypeDeclaration());
	}

	@Test
	public void test_mainMethodPresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "public static void main(String[] args) {"
				+ "	System.out.println(\"Hello\" + test());" + "}" + "" + "public static String test() {"
				+ "	return \"asdf\";" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected),
				defaultFixture.getTypeDeclaration());
	}

	@Test
	public void test_mainMethodWithoutParametersPresent_shouldTransform() throws Exception {
		String actual = "public static void asdf() {"
				+ "	System.out.println(\"method named main, but wrong signature\");" + "}" + ""
				+ "public static void test() {" + "	System.out.println(\"test method\");" + "}";

		String expected = "private TestCU() {" + "}" + "" + actual;

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actual);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, expected),
				defaultFixture.getTypeDeclaration());

	}

	@Test
	public void test_constructorAlreadyPresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "public TestCU() {" + "}" + "public static void test() {" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected),
				defaultFixture.getTypeDeclaration());
	}

	@Test
	public void test_onlyStaticFieldsAndMethodsArePresent_shouldTransform() throws Exception {
		String actual = "public static String testString;" + "public static Integer testInteger;" + ""
				+ "public static void test() {" + "}" + "" + "public static String combine() {"
				+ "	return testString + testInteger;" + "}";

		String expected = "private TestCU() {" + "}" + actual;

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actual);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, expected),
				defaultFixture.getTypeDeclaration());
	}

	@Test
	public void test_nonStaticFieldsArePresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "public String testString;" + "public static Integer testInteger;" + ""
				+ "public static void test() {" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected),
				defaultFixture.getTypeDeclaration());
	}

	@Test
	public void test_noMethodsAndNoFieldsArePresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "class InnerTestCU {" + "}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected),
				defaultFixture.getTypeDeclaration());
	}

	@Test
	public void test_onlyStaticFiledsArePresent_shouldTransform() throws Exception {
		String actual = "public static Integer field;";

		String expected = "private TestCU() {" + "}" + actual;

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actual);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, expected),
				defaultFixture.getTypeDeclaration());
	}

	@Test
	public void test_onlyStaticAndNonStaticFiledsArePresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "public static Integer field;" + "public String stringField;";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected);

		visitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(visitor);

		assertMatch(ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, actualAndExpected),
				defaultFixture.getTypeDeclaration());
	}
}
