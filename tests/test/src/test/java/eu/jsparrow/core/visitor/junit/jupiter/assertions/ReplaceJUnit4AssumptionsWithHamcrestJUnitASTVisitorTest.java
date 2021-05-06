package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitorTest
		extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		addDependency("org.hamcrest", "hamcrest-library", "1.3");
		addDependency("org.hamcrest", "hamcrest-core", "1.3");
		setDefaultVisitor(new ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_unqualifiedAssumeThat_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeThat", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(new Object(), notNullValue());\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");
		assertChange(original, original, expectedImports);
	}

	@Test
	public void visit_AssumeThatWithMessage_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeThat", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(\"Assumed that {new Object} is not null.\", new Object(), notNullValue());\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");
		assertChange(original, original, expectedImports);
	}

	@Test
	public void visit_qualifiedAssumeThat_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assume.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assume.assumeThat(new Object(), notNullValue());\n" +
				"	}";
		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(new Object(),notNullValue());\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assume;",
				"import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_unqualifiedAssumeNoException_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeThat", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(new Object(), notNullValue());\n" +
				"	}";
		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(new Object(), notNullValue());\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");
		assertChange(original, expected, expectedImports);
	}

}
