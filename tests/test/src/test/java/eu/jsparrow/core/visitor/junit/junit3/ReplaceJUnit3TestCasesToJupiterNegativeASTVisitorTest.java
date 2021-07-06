package eu.jsparrow.core.visitor.junit.junit3;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class ReplaceJUnit3TestCasesToJupiterNegativeASTVisitorTest
		extends UsesJDTUnitFixture {

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
				"	class TestWithJupiterAnnotation extends TestCase {\n" +
				"	\n" +
				"		@DisplayName(\"test\")\n" +
				"		void test() {\n" +
				"			assertNotNull(new Object());\n" +
				"		}\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_SuperCountTestCases_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	public class SuperCountTestCasesInvocationTest extends TestCase {\n"
				+ "		\n"
				+ "		void test() {\n"
				+ "			assertEquals(1, super.countTestCases());\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_TestCaseClassLiteral_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	public class TestCaseClassLiteralTest extends TestCase {\n"
				+ "\n"
				+ "		void test() {\n"
				+ "			assertEquals(\"junit.framework.TestCase\", TestCase.class.getName());\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AssertNotNullMethodReference_shouldNotTransform() throws Exception {
		defaultFixture.addImport("java.util.function.Consumer");
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.addImport("junit.framework.Assert");
		String original = "" +
				"	class AssertNotNullExpressionMethodReferenceTest extends TestCase {\n"
				+ "\n"
				+ "		void test() {\n"
				+ "			Consumer<Object> asserter = Assert::assertNotNull;\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_QualifiedJupiterDisabledAnnotation_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	@org.junit.jupiter.api.Disabled\n"
				+ "	public class QualifiedJupiterDisabledAnnotationTest extends TestCase {\n"
				+ "\n"
				+ "		void test() {\n"
				+ "			assertNotNull(new Object());\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_TestCaseAsTypeArgument_shouldNotTransform() throws Exception {
		defaultFixture.addImport("java.util.List");
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	public class TestCaseAsTypeArgument extends TestCase {\n"
				+ "		void test() {\n"
				+ "			List<List<TestCase>> listOfListOfTestCases = null;\n"
				+ "			assertNull(listOfListOfTestCases);\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_TestCaseImplementingProtectable_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.Protectable");
		defaultFixture.addImport("junit.framework.TestCase");
		String original = "" +
				"	public abstract class TestCaseImplementingProtectable extends TestCase implements Protectable {\n"
				+ "		void test() {\n"
				+ "			assertNotNull(new Object());\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_ClassExtendingAssert_shouldNotTransform() throws Exception {
		defaultFixture.addImport("junit.framework.Assert");
		String original = "" +
				"	public class ClassExtendingAssertTest extends Assert {\n"
				+ "		void test() {\n"
				+ "			assertNotNull(new Object());\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	// @Test
	// public void visit__shouldNotTransform() throws Exception {
	// defaultFixture.addImport("junit.framework.TestCase");
	// String original = "" +
	// "";
	//
	// assertNoChange(original);
	// }

	// @Test
	// public void visit__shouldNotTransform() throws Exception {
	// defaultFixture.addImport("junit.framework.TestCase");
	// String original = "" +
	// "";
	//
	// assertNoChange(original);
	// }

}
