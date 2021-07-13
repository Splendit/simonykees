package eu.jsparrow.core.visitor.junit.junit3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
			.createJUnitJupiterConfigurationValues();
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
		defaultFixture.setSuperClassType("TestCase");
		String original = "@DisplayName(\"test\")\n" +
				PUBLIC_VOID_TEST + "() {\n" +
				"	assertNotNull(new Object());\n" +
				"}";
		String compilationUnitFormat = ""
				+ "package %s;\n"
				+ "import junit.framework.TestCase;\n"
				+ "import org.junit.jupiter.api.DisplayName;\n"
				+ "public class %s extends TestCase {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, compilationUnitFormat);
	}

	@Test
	public void visit_ImportOfJupiterOnDemand_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.addImport("org.junit.jupiter.api", false, true);
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
		defaultFixture.setSuperClassType("TestCase");
		String original = "" +
				PUBLIC_VOID_TEST + "() {\n" +
				"	assertEquals(1, super.countTestCases());\n" +
				"}";
		String compilationUnitFormat = ""
				+ "package %s;\n"
				+ "import junit.framework.TestCase;\n"
				+ "public class %s extends TestCase {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, compilationUnitFormat);

	}

	@Test
	public void visit_TestCaseClassLiteral_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		String original = "" +
				PUBLIC_VOID_TEST + "() {\n" +
				"	assertEquals(\"junit.framework.TestCase\", TestCase.class.getName());\n" +
				"}";

		String compilationUnitFormat = ""
				+ "package %s;\n"
				+ "import junit.framework.TestCase;\n"
				+ "public class %s extends TestCase {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, compilationUnitFormat);
	}

	@Test
	public void visit_AssertNotNullMethodReference_shouldNotTransform() throws Exception {
		defaultFixture.addImport("java.util.function.Consumer");
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.addImport("junit.framework.Assert");
		defaultFixture.setSuperClassType("TestCase");
		String original = "" +
				PUBLIC_VOID_TEST + "() {\n" +
				"	Consumer<Object> asserter = Assert::assertNotNull;\n" +
				"}";

		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import java.util.function.Consumer;\n"
				+ "import junit.framework.TestCase;\n"
				+ "import junit.framework.Assert;\n"
				+ "public class %s extends TestCase {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_AssertNotNullInLambda_shouldNotTransform() throws Exception {
		defaultFixture.addImport("java.util.function.Consumer");
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		String original = "" +
				PUBLIC_VOID_TEST + "() {\n" +
				"	Consumer<Object> asserter = () ->  assertNotNull();\n" +
				"}";

		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import java.util.function.Consumer;\n"
				+ "import junit.framework.TestCase;\n"
				+ "public class %s extends TestCase {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, expectedCompilationUnitFormat);
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
		defaultFixture.setSuperClassType("TestCase");
		String original = PUBLIC_VOID_TEST + "() {\n" +
				"	List<List<TestCase>> listOfListOfTestCases = null;\n" +
				"	assertNull(listOfListOfTestCases);\n" +
				"}";

		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import java.util.List;\n"
				+ "import junit.framework.TestCase;\n"
				+ "public class %s extends TestCase {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_TestCaseImplementingProtectable_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.Protectable");
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		defaultFixture.setSuperInterfaceType("Protectable");
		String protectedMethodImpl = "@Override public void protect() throws Throwable {}\n";
		defaultFixture.addMethodDeclarationFromString(protectedMethodImpl);
		String original = "" +
				PUBLIC_VOID_TEST + "() {\n" +
				"	assertNotNull(new Object());\n" +
				"}";
		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import junit.framework.Protectable;\n"
				+ "import junit.framework.TestCase;\n"
				+ "public class %s extends TestCase implements Protectable {\n"
				+ protectedMethodImpl
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_ClassExtendingAssert_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.Assert");
		defaultFixture.setSuperClassType("Assert");
		String original = "" +
				PUBLIC_VOID_TEST + "() {\n" +
				"	assertNotNull(new Object());\n" +
				"}";

		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import junit.framework.Assert;\n"
				+ "public class %s extends Assert {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, expectedCompilationUnitFormat);
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
		defaultFixture.setSuperClassType("TestCase");
		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import junit.framework.TestCase;\n"
				+ "public class %s extends TestCase {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(testMethod, expectedCompilationUnitFormat);
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
		defaultFixture.setSuperClassType("TestCase");
		String original = "" +
				PUBLIC_VOID_TEST + "() {\n" +
				"	" + testMethodContent + "\n" +
				" }";

		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import junit.framework.TestCase;\n"
				+ "public class %s extends TestCase {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_SuperConstructorInvocation_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	public static class ExampleTestCase extends TestCase {\n" +
				"\n" +
				"		public ExampleTestCase(String name) {\n" +
				"			super(name);\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"@Override\n" +
					"protected TestResult createResult() {\n" +
					"	return null;\n" +
					"}",
			"" +
					"@Override\n" +
					"public TestResult run() {\n" +
					"	return null;\n" +
					"}",
			"" +
					"@Override\n" +
					"public void run(TestResult result) {\n" +
					"	\n" +
					"}"
	})
	public void visit_TestCaseMethodsWithOverrideAnnotation_shouldNotTransform(String methodDeclaration)
			throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.addImport("junit.framework.TestResult");
		defaultFixture.setSuperClassType("TestCase");

		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"import junit.framework.TestCase;\n" +
				"import junit.framework.TestResult;\n" +
				"public class %s extends TestCase {\n" +
				"	%s \n" +
				"}";

		assertNoCompilationUnitChange(methodDeclaration, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_MethodBindingNotResolved_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		String original = "" +
				PUBLIC_VOID_TEST + "() {\n" +
				"	assertObjectNotNull(new Object());\n" +
				"}";

		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import junit.framework.TestCase;\n"
				+ "public class %s extends TestCase {\n"
				+ "	%s \n"
				+ "}";

		assertNoCompilationUnitChange(original, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_AssertNotNullInInitializer_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	" + PUBLIC_STATIC_CLASS_EXAMPLE_TEST_EXTENDS_TEST_CASE + " {\n" +
				"\n" +
				"		{\n" +
				"			assertNotNull(new Object());\n" +
				"		}\n" +
				"\n" +
				"		" + PUBLIC_VOID_TEST + "() {\n" +
				"			assertNotNull(new Object());\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AmbiguousArgumentTypes_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		defaultFixture.addMethodDeclarationFromString("" +
				"<RET> RET getGenericReturnValue() {\n" +
				"	return (RET) Byte.valueOf((byte) 0);\n" +
				"}\n");

		String original = "" +
				"public void test() {\n" +
				"	assertEquals(getGenericReturnValue(), getGenericReturnValue());\n" +
				"}";
		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"import junit.framework.TestCase;\n" +
				"public class %s extends TestCase {\n" +
				"	<RET>RET getGenericReturnValue(){\n" +
				"		return (RET)Byte.valueOf((byte)0);\n" +
				"	}\n" +
				"	%s \n" +
				"}";

		assertNoCompilationUnitChange(original, expectedCompilationUnitFormat);
	}
}
