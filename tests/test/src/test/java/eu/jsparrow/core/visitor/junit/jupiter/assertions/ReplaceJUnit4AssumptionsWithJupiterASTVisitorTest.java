package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class ReplaceJUnit4AssumptionsWithJupiterASTVisitorTest
		extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		setDefaultVisitor(new ReplaceJUnit4AssumptionsWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_qualifiedAssumeTrue_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume");
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assume.assumeTrue(1L == 1L);\n" +
				"	}";
		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeTrue(1L == 1L);\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assume;",
				"import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assumptions.assumeTrue;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_qualifiedAssumeTrueWithMessage_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume");
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assume.assumeTrue(\"Assumption that 1L == 1L\", 1L == 1L);\n" +
				"	}";
		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeTrue(1L == 1L,\"Assumption that 1L == 1L\");\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assume;",
				"import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assumptions.assumeTrue;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeTrueWithoutQualifier_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeTrue", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assumeTrue(1L == 1L);\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assumeTrue(1L == 1L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assumptions.assumeTrue;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_twoStaticAssumeMethodImports_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeTrue", true, false);
		defaultFixture.addImport("org.junit.Assume.assumeFalse", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assumeFalse(1L == 0L);\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assumeFalse(1L == 0L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assumptions.assumeFalse;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_assumeTrueInvocationWithoutQualifier_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeTrue", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"@Test\n"
				+ "	void test() {\n"
				+ "		assumeTrue(1L == 1L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assumptions.assumeTrue;");
		assertChange(original, original, expectedImports);
	}

	@Test
	public void visit_assumeTrueInvocationWithoutQualifierWithMessage_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeTrue", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeTrue(\"1L should be equal to 1L\", 1L == 1L);\n" +
				"	}";

		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeTrue(1L == 1L, \"1L should be equal to 1L\");\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assumptions.assumeTrue;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_needingAssumptionsQualifier_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeTrue", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	void methodWithoutTestAnnotation() {\n"
				+ "		assumeTrue(1L == 1L);\n"
				+ "	}\n"
				+ "\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "		assumeTrue(1L == 1L);\n"
				+ "	}";

		String expected = "" +
				"	void methodWithoutTestAnnotation() {\n"
				+ "		assumeTrue(1L == 1L);\n"
				+ "	}\n"
				+ "\n"
				+ "	@Test\n"
				+ "	void test() {\n"
				+ "		Assumptions.assumeTrue(1L == 1L);\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Assumptions;",
				"import org.junit.jupiter.api.Test;",
				"import static org.junit.Assume.assumeTrue;");
		assertChange(original, expected, expectedImports);
	}
}
