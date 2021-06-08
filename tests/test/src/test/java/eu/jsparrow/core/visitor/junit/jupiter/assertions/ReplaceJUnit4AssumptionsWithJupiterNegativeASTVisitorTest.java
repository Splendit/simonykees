package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class ReplaceJUnit4AssumptionsWithJupiterNegativeASTVisitorTest
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
	public void visit_assumeThat_shouldNotTransform() throws Exception {
		addDependency("org.hamcrest", "hamcrest-library", "1.3");
		addDependency("org.hamcrest", "hamcrest-core", "1.3");

		defaultFixture.addImport("org.hamcrest.Matchers.equalToIgnoringCase", true, false);
		defaultFixture.addImport("org.junit.Assume.assumeThat", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = ""
				+ "		@Test\n"
				+ "		public void test() {\n"
				+ "			assumeThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "		}\n"
				+ "	";

		assertNoChange(original);
	}

	@Test
	public void visit_assumeNotNull_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = ""
				+ "		@Test\n"
				+ "		public void test() {\n"
				+ "			assumeNotNull(new Object(), new Object());\n"
				+ "		}\n"
				+ "	";

		assertNoChange(original);
	}

	@Test
	public void visit_AssumeTrueNotInTestMethod_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeTrue", true, false);
		String original = ""
				+ "	void test() {\n"
				+ "		assumeTrue(1L == 1L);\n"
				+ "	}";
		assertNoChange(original);
	}
}