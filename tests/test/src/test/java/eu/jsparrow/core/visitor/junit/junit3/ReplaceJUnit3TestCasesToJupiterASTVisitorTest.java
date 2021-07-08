package eu.jsparrow.core.visitor.junit.junit3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class ReplaceJUnit3TestCasesToJupiterASTVisitorTest extends UsesJDTUnitFixture {

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
	public void visit_fullyQualifiedAssertMethod_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	public static class ExampleTestCase extends TestCase {\n" +
				"	\n" +
				"		public void test() {\n" +
				"			junit.framework.Assert.assertTrue(true);\n" +
				"		}\n" +
				"	}";

		String expected = "" +
				"	public static class ExampleTestCase {\n" +
				"	\n" +
				"		@Test\n" +
				"		public void test() {\n" +
				"			assertTrue(true);\n" +
				"		}\n" +
				"	}";
		assertChange(original, expected);
	}

	@Test
	public void visit_UnqualifiedTestCaseFieldAccess_shouldTransform() throws Exception {
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
				+ "			assertEquals(1,number);\n"
				+ "		}\n"
				+ "	}";
		assertChange(original, expected);

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

}
