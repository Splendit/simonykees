package eu.jsparrow.core.visitor.junit.junit3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class ReplaceJUnit3TestCasesToJupiterASTVisitorTest extends UsesJDTUnitFixture {

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
}
