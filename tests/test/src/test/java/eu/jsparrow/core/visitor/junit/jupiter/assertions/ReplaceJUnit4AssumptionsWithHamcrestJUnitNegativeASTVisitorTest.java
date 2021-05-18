package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class ReplaceJUnit4AssumptionsWithHamcrestJUnitNegativeASTVisitorTest
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
	public void visit_AssumeThatNotDeclaredInJUnit4Assume_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.hamcrest.Matchers.equalToIgnoringCase", true, false);
		defaultFixture.addImport("org.hamcrest.Matcher");
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	static <T> void assumeThat(T actual, Matcher<T> matcher) {\n"
				+ "\n"
				+ "	}\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	public void visit_notResolvedAssumeThat_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(new Object(), notNullValue());\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_ConditionalAssumeNotNull_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	Object[] objects;\n" +
				"\n" +
				"	@Test\n" +
				"	public void test() {\n" +
				"		if (true)\n" +
				"			assumeNotNull(objects);\n" +
				"	}";

		assertNoChange(original);
	}
	
	@Test
	public void visit_AssumeTrue_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeTrue", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	@Test\n"
				+ "	void test() {\n"
				+ "		assumeTrue(1L == 1L);\n"
				+ "	}";
		assertNoChange(original);
	}
}