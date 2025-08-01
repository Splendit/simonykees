package eu.jsparrow.core.visitor.junit.junit3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

public class ReplaceJUnit3TestCasesToJupiterASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
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
	public void visit_fullyQualifiedAssertMethod_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");

		String expected = "" +
				"@Test\n" +
				"public void test() {\n" +
				"	assertTrue(true);\n" +
				"}";
		String original = "" +
				"public void test() {\n" +
				"	junit.framework.Assert.assertTrue(true);\n" +
				"}";

		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import static org.junit.jupiter.api.Assertions.assertTrue;\n"
				+ "import org.junit.jupiter.api.Test;"
				+ "public class %s {\n"
				+ "	%s \n"
				+ "}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_UnqualifiedTestCase_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		String original = ""
				+ "public void test() {\n"
				+ "	int number = 1;\n"
				+ "	assertEquals(1, number);\n"
				+ "}";

		String expected = ""
				+ "@Test\n"
				+ "public void test(){\n"
				+ "	int number = 1;\n"
				+ "	assertEquals(1,number);\n"
				+ "}";
		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import static org.junit.jupiter.api.Assertions.assertEquals;\n"
				+ "import org.junit.jupiter.api.Test;"
				+ "public class %s {\n"
				+ "	%s \n"
				+ "}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);

	}

	@Test
	public void visit_TestCaseThisFieldAccess_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	public static class UnqualifiedFieldAccessTest extends TestCase {\n"
				+ "		private int number = 1;\n"
				+ "\n"
				+ "		public void test() {\n"
				+ "			assertEquals(1, this.number);\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public static class UnqualifiedFieldAccessTest {\n"
				+ "		private int number = 1;\n"
				+ "\n"
				+ "		@Test"
				+ "		 public void test(){\n"
				+ "			assertEquals(1, this.number);\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);

	}

	@Test
	public void visit_UnqualifiedFieldAccess_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	public static class UnqualifiedFieldAccessTest extends TestCase {\n"
				+ "		private int number = 1;\n"
				+ "\n"
				+ "		public void test() {\n"
				+ "			assertEquals(1, number);\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public static class UnqualifiedFieldAccessTest {\n"
				+ "		private int number = 1;\n"
				+ "\n"
				+ "		@Test"
				+ "		 public void test(){\n"
				+ "			assertEquals(1, number);\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);

	}

	@Test
	public void visit_SuperConstructorForObject_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	class NoTestCase {\n"
				+ "		NoTestCase() {\n"
				+ "			super();\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	public static class ExampleTestCase extends TestCase {\n"
				+ "\n"
				+ "	}";

		String expected = "" +
				"	class NoTestCase {\n"
				+ "		NoTestCase() {\n"
				+ "			super();\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	public static class ExampleTestCase {\n"
				+ "\n"
				+ "	}";
		assertChange(original, expected);
	}

	@Test
	public void visit_AmbiguousAssertTrue_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		defaultFixture.addMethodDeclarationFromString("" +
				"void assertTrue() {\n" +
				"}\n");

		String original = "" +
				"public void test() {\n" +
				"	assertTrue(true);\n" +
				"}";

		String expected = "" +
				"@Test\n" +
				"public void test() {\n" +
				"	Assertions.assertTrue(true);\n" +
				"}";
		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"import org.junit.jupiter.api.Test;\n" +
				"import org.junit.jupiter.api.Assertions;\n" +
				"public class %s {\n" +
				"	void assertTrue(){\n" +
				"	}\n" +
				"	%s \n" +
				"}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"" +
					"public int countTestCases() {\n" +
					"	return 1;\n" +
					"}",
			"" +
					"public String getName() {\n" +
					"	return \"ExampleTest\";\n" +
					"}",
			"" +
					"public void runBare() throws Throwable {\n" +
					"}",
			"" +
					"protected void runTest() throws Throwable {\n" +
					"}",
			"" +
					"public void setName(String name) {\n" +
					"}"
	})
	public void visit_TestCaseMethodOverrideAnnotations_shouldTransform(String methodDeclaration) throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");

		String original = "@Override\n" + methodDeclaration;

		String expected = methodDeclaration;

		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"public class %s {\n" +
				"	%s \n" +
				"}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_AssertEqualsForStringsWithoutMessage_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		String original = ""
				+ "public void test() {\n"
				+ "	String helloWorld = \"HelloWorld!\";\n"
				+ "	assertEquals(\"HelloWorld!\", helloWorld);\n"
				+ "}";

		String expected = ""
				+ "@Test\n"
				+ "public void test(){\n"
				+ "	String helloWorld = \"HelloWorld!\";\n"
				+ "	assertEquals(\"HelloWorld!\", helloWorld);\n"
				+ "}";
		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import static org.junit.jupiter.api.Assertions.assertEquals;\n"
				+ "import org.junit.jupiter.api.Test;"
				+ "public class %s {\n"
				+ "	%s \n"
				+ "}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);

	}

	@Test
	public void visit_TestCaseGetNumberInvocation_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		defaultFixture.addMethodDeclarationFromString("" +
				"int getNumber() {\n" +
				"	return 1;\n" +
				"}\n");

		String original = "" +
				"public void test() {\n" +
				"	assertEquals(1, getNumber());\n" +
				"}";

		String expected = "" +
				"@Test\n" +
				"public void test() {\n" +
				"	assertEquals(1, getNumber());\n" +
				"}";

		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
				"import org.junit.jupiter.api.Test;" +
				"public class %s {\n" +
				"	int getNumber() {\n" +
				"		return 1;\n" +
				"	}\n" +
				"	%s \n" +
				"}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);

	}

	@Test
	public void visit_SuperSetUpInvocation_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");

		String original = "" +
				"	@Override\n" +
				"	protected void setUp() throws Exception {\n" +
				"		super.setUp();\n" +
				"	}";

		String expected = "" +
				"	@BeforeEach\n" +
				"	protected void setUp() throws Exception {\n" +
				"	}";

		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"import org.junit.jupiter.api.BeforeEach;\n" +
				"public class %s {\n" +
				"	%s \n" +
				"}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_SuperTearDownInvocation_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");

		String original = "" +
				"	@Override\n" +
				"	protected void tearDown() throws Exception {\n" +
				"		super.tearDown();\n" +
				"	}";

		String expected = "" +
				"	@AfterEach\n" +
				"	protected void tearDown() throws Exception {\n" +
				"	}";

		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"import org.junit.jupiter.api.AfterEach;\n" +
				"public class %s {\n" +
				"	%s \n" +
				"}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);
	}

	@Test
	public void visit_ParameterizedMethodInvocationWithTypeArguments_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		defaultFixture.addMethodDeclarationFromString("" +
				"	@SuppressWarnings(\"unchecked\")\n" +
				"	<T> T getGenericReturnValue() {\n" +
				"		return (T) Byte.valueOf((byte) 0);\n" +
				"	}\n");

		String original = "" +
				"public void test() {\n" +
				"	assertEquals(this.<String>getGenericReturnValue(), this.<String>getGenericReturnValue());\n" +
				"}";

		String expected = "" +
				"@Test\n" +
				"public void test() {\n" +
				"	 assertEquals(this.<String>getGenericReturnValue(),this.<String>getGenericReturnValue());\n" +
				"}";

		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
				"import org.junit.jupiter.api.Test;" +
				"public class %s {\n" +
				"  @SuppressWarnings(\"unchecked\") <T>T getGenericReturnValue(){\n" +
				"    return (T)Byte.valueOf((byte)0);\n" +
				"  }\n" +
				"	%s \n" +
				"}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);

	}

	@Test
	public void visit_ListWithAmbiguousItemType_shouldTransform() throws Exception {
		defaultFixture.addImport("java.util.Arrays");
		defaultFixture.addImport("java.util.List");
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		defaultFixture.addMethodDeclarationFromString("" +
				"	@SuppressWarnings(\"unchecked\")\n" +
				"	<T> List<T> getListWithAmbiguousItemType() {\n" +
				"		return (List<T>) Arrays.asList(null);\n" +
				"	}\n");

		String original = "" +
				"public void test() {\n" +
				"	assertEquals(getListWithAmbiguousItemType(), getListWithAmbiguousItemType());\n" +
				"}";

		String expected = "" +
				"@Test\n" +
				"public void test() {\n" +
				"	 assertEquals(getListWithAmbiguousItemType(), getListWithAmbiguousItemType());\n" +
				"}";

		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
				"import java.util.Arrays;\n" +
				"import java.util.List;\n" +
				"import org.junit.jupiter.api.Test;\n" +
				"public class %s {\n" +
				"  @SuppressWarnings(\"unchecked\") <T>List<T> getListWithAmbiguousItemType(){\n" +
				"    return (List<T>)Arrays.asList(null);\n" +
				"  }\n" +
				"	%s \n" +
				"}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);

	}

	@Test
	public void visit_ImportOfJupiterOnDemand_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.addImport("org.junit.jupiter.api", false, true);
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");

		String original = "" +
				"	@DisplayName(\"test\")\n" +
				"	public void test() {\n" +
				"		assertEquals(1, 1);\n" +
				"	}";

		String expected = "" +
				"	@Test" +
				"	@DisplayName(\"test\")\n" +
				"	public void test() {\n" +
				"		assertEquals(1, 1);\n" +
				"	}";

		String expectedCompilationUnitFormat = "" +
				"package %s;\n" +
				"import static org.junit.jupiter.api.Assertions.assertEquals;\n" +
				"import org.junit.jupiter.api.*;\n" +
				"public class %s {\n" +
				"	%s \n" +
				"}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);

	}

	@Test
	public void visit_SynchronizedStatementWithTestCaseSubclass_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"public static class SynchronizedStatementInSetupTest extends TestCase {\n" +
				"	@Override\n" +
				"	public void setUp() throws Exception {\n" +
				"		synchronized (SynchronizedStatementInSetupTest.class) {\n" +
				"		}\n" +
				"	}\n" +
				"}";

		String expected = "" +
				"public static class SynchronizedStatementInSetupTest {\n" +
				"	@BeforeEach \n" +
				"	public void setUp() throws Exception {\n" +
				"		synchronized (SynchronizedStatementInSetupTest.class) {\n" +
				"		}\n" +
				"	}\n" +
				"}";

		assertChange(original, expected);

	}
	
	
	@Test
	public void visit_MethodWithLabeledStatements_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");
		
		defaultFixture.addMethodDeclarationFromString("" +
				"public void useLabels() {\n"
				+ "\n"
				+ "		xLabel1: for (int i = 0; i < 10; i++) {\n"
				+ "			if (i == 3) {\n"
				+ "				break xLabel1;\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "		for (int i = 0; i < 5; i++) {\n"
				+ "			xLabel2: for (int j = 0; j < 5; j++) {\n"
				+ "				continue xLabel2;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}");
		
		String original = ""
				+ "public void test() {\n"
				+ "	int number = 1;\n"
				+ "	assertEquals(1, number);\n"
				+ "}";

		String expected = ""
				+ "@Test\n"
				+ "public void test(){\n"
				+ "	int number = 1;\n"
				+ "	assertEquals(1,number);\n"
				+ "}";
		String expectedCompilationUnitFormat = ""
				+ "package %s;\n"
				+ "import static org.junit.jupiter.api.Assertions.assertEquals;\n"
				+ "import org.junit.jupiter.api.Test;"
				+ "public class %s {\n"
				+ "public void useLabels() {\n"
				+ "\n"
				+ "		xLabel1: for (int i = 0; i < 10; i++) {\n"
				+ "			if (i == 3) {\n"
				+ "				break xLabel1;\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "		for (int i = 0; i < 5; i++) {\n"
				+ "			xLabel2: for (int j = 0; j < 5; j++) {\n"
				+ "				continue xLabel2;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}"
				+ "	%s \n"
				+ "}";

		assertCompilationUnitMatch(original, expected, expectedCompilationUnitFormat);

	}
}
