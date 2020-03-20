package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.JdtUnitFixtureClass;

@SuppressWarnings("nls")
public class HideDefaultConstructorInUtilityClassesASTVisitorTest extends UsesJDTUnitFixture {

	private static final String ADDED_CONSTRUCTOR = "private  " + DEFAULT_TYPE_DECLARATION_NAME + "() {"
												  + "	throw new IllegalStateException(\"Utility class\");"
												  + "}";

	@BeforeEach
	public void setUpDefaultVisitor() throws Exception {
		setDefaultVisitor(new HideDefaultConstructorInUtilityClassesASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void test_allCriteriaSatisfied_shouldTransform() throws Exception {
		String actual = "public static void sayHallo() {" + "    System.out.println(\"Hallo\");" + "}";
		String expected = ADDED_CONSTRUCTOR + actual;

		assertChange(actual, expected);
	}

	@Test
	@Disabled
	/*
	 * TODO: search engine works for normal eclipse instances but for some
	 * reason not with the fixture project. This has to be investigated and fixed
	 */
	public void test_defaultConstructorInvokedInOtherClass_shouldNotTransform() throws Exception {
		String actualAndExpected = "public static void test() {" + "}";

		JdtUnitFixtureClass testClass2 = fixtureProject.addCompilationUnit("Test2");
		testClass2.addMethod("testMethodWithDefaultConstructorInvocation",
				DEFAULT_TYPE_DECLARATION_NAME + " t = new " + DEFAULT_TYPE_DECLARATION_NAME + "();");

		assertNoChange(actualAndExpected);
	}

	@Test
	public void test_nonStaticMethodPresent_shouldNotTransform() throws Exception {
		String acutalAndExpected = "public static void test() {" + "	System.out.println(\"test\");" + "}" + ""
				+ "public String nonStaticMethod() {" + "	return \"non-static method\";" + "}";

		assertNoChange(acutalAndExpected);
	}

	@Test
	public void test_mainMethodPresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "public static void main(String[] args) {"
				+ "	System.out.println(\"Hello\" + test());" + "}" + "" + "public static String test() {"
				+ "	return \"asdf\";" + "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void test_mainMethodWithoutParametersPresent_shouldTransform() throws Exception {
		String actual = "public static void asdf() {"
				+ "	System.out.println(\"method named main, but wrong signature\");" + "}" + ""
				+ "public static void test() {" + "	System.out.println(\"test method\");" + "}";

		String expected = ADDED_CONSTRUCTOR + actual;

		assertChange(actual, expected);

	}

	@Test
	public void test_constructorAlreadyPresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "public TestCU() {" + "}" + "public static void test() {" + "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void test_onlyStaticFieldsAndMethodsArePresent_shouldTransform() throws Exception {
		String actual = "public static String testString;" + "public static Integer testInteger;" + ""
				+ "public static void test() {" + "}" + "" + "public static String combine() {"
				+ "	return testString + testInteger;" + "}";

		String expected = ADDED_CONSTRUCTOR + actual;

		assertChange(actual, expected);
	}

	@Test
	public void test_nonStaticFieldsArePresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "public String testString;" + "public static Integer testInteger;" + ""
				+ "public static void test() {" + "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void test_noMethodsAndNoFieldsArePresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "class InnerTestCU {" + "}";

		assertNoChange(actualAndExpected);
	}

	@Test
	public void test_onlyStaticFieldsArePresent_shouldTransform() throws Exception {
		String actual = "public static Integer field;";

		String expected = ADDED_CONSTRUCTOR + actual;

		assertChange(actual, expected);
	}

	@Test
	public void test_onlyStaticAndNonStaticFieldsArePresent_shouldNotTransform() throws Exception {
		String actualAndExpected = "public static Integer field;" + "public String stringField;";

		assertNoChange(actualAndExpected);
	}
}
