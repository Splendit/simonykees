package eu.jsparrow.core.visitor.junit.junit3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class ReplaceJUnit3TestCasesToJupiterNegativeASTVisitorTest
		extends UsesJDTUnitFixture {

	private static final String PUBLIC_VOID_TEST = "public void test";
	private static final String PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE = "public static class ExampleTest extends TestCase";

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "3.8.2");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		Junit3MigrationConfiguration configuration = new Junit3MigrationConfigurationFactory()
			.createJUnit4ConfigurationValues();
		setDefaultVisitor(new ReplaceJUnit3TestCasesASTVisitor(configuration));
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_ImportOfJupiterAnnotation_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.addImport("org.junit.jupiter.api.DisplayName");
		String original = "" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " {\n" +
				"\n" +
				"		@DisplayName(\"test\")\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			assertNotNull(new Object());\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_SuperCountTestCases_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " {\n" +
				"\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			assertEquals(1, super.countTestCases());\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_TestCaseClassLiteral_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " {\n" +
				"\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			assertEquals(\"junit.framework.TestCase\", TestCase.class.getName());\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssertNotNullMethodReference_shouldNotTransform() throws Exception {
		defaultFixture.addImport("java.util.function.Consumer");
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.addImport("junit.framework.Assert");
		String original = "" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " {\n" +
				"\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			Consumer<Object> asserter = Assert::assertNotNull;\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_QualifiedJupiterDisabledAnnotation_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	@org.junit.jupiter.api.Disabled\n" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " {\n" +
				"\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			assertNotNull(new Object());\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_TestCaseAsTypeArgument_shouldNotTransform() throws Exception {
		defaultFixture.addImport("java.util.List");
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " {\n" +
				"\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			List<List<TestCase>> listOfListOfTestCases = null;\n" +
				"			assertNull(listOfListOfTestCases);\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_TestCaseImplementingProtectable_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.Protectable");
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " implements Protectable {\n" +
				"\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			assertNotNull(new Object());\n" +
				"		}\n" +
				"\n" +
				"		@Override\n" +
				"		public void protect() throws Throwable {\n" +
				"\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_ClassExtendingAssert_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.Assert");
		String original = "" +
				"	public static class ClassExtendingAssertTest extends Assert {\n" +
				"	\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			assertNotNull(new Object());\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"static",
			"public",
			"public static abstract"
	})
	public void visit_TestCaseModifiersNotAsRequired_shouldNotTransform(String modifiers) throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	" + modifiers + " class ExampleTestCase extends TestCase {\n" +
				"	\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			assertTrue(true);\n" +
				"		}\n" +
				"	}";
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"		public void test(String s) {\n" +
					"			assertTrue(true);\n" +
					"		}",
			"" +
					"		public void isNoTest() {\n" +
					"			assertTrue(true);\n" +
					"		}",
			"" +
					"		public int test() {\n" +
					"			assertTrue(true);\n" +
					"			return 1;\n" +
					"		}",
			"" +
					"		void test() {\n" +
					"			assertTrue(true);\n" +
					"		}"
	})
	public void visit_MethodDeclarationsNotTestMethods_shouldNotTransform(String testMethod) throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " {\n" +
				"	\n" + testMethod + "\n" +
				"	}";
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"			Object o = new Object();\n" +
					"			failNotEquals(\"Fails when Objects are not equal.\", o, o);",
			"" +
					"			Object o = new Object();\n" +
					"			failNotSame(\"Fails when Objects are not same.\", o, o);",
			"" +
					"			failSame(\"Fails when both Objects are same.\");",
			"" +
					"			Object[] objectArray = new Object[] {};\n" +
					"			assertEquals(\"Object arrays expected to be equal.\", objectArray, objectArray);\n",
			"" +
					"			Object[] objectArray = new Object[] {};\n" +
					"			assertEquals(objectArray, objectArray);\n"
	})
	public void visit_NotSupportedJUnit3Assertions_shouldNotTransform(String testMethodContent) throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " {\n" +
				"\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"\n" + testMethodContent + "\n" +
				"	 	}\n" +
				" }";

		assertNoChange(original);
	}

	@Disabled("Template")
	@Test
	public void visit__shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	 public static class ExampleTestCase extends TestCase {\n" +
				" \n" +
				"	 	public void test() {\n" +
				"		 assertTrue(true);\n" +
				"	 	}\n" +
				" }";
		assertNoChange(original);
	}

}
