package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class ReplaceJUnit4AssertionsWithJupiterNegativeASTVisitorTest
		extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		setDefaultVisitor(new ReplaceJUnit4AssertionsWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_assertEqualsInTestMethodOfAnonymousClass_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	void test() {\n"
				+ "		Runnable r = new Runnable() {\n"
				+ "			@Test\n"
				+ "			@Override\n"
				+ "			public void run() {\n"
				+ "				assertEquals(10L, 10L);\n"
				+ "			}\n"
				+ "		};\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertEqualsInTestMethodOfEnum_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	enum TestsEnum {\n"
				+ "		TEST;\n"
				+ "\n"
				+ "		@Test\n"
				+ "		void test() {\n"
				+ "			assertEquals(10L, 10L);\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertEqualsAsLambdaBody_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		String original = "" +
				"	void test() {\n"
				+ "		Runnable r = () -> assertEquals(10L, 10L);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertEqualsWithQualifierAsLambdaBody_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		String original = "" +
				"	void test() {\n"
				+ "		Runnable r = () -> Assert.assertEquals(10L, 10L);\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertEqualsInTestMethodOfLocalClass_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	void test() {\n"
				+ "		class LocalClass {\n"
				+ "			@Test\n"
				+ "			void test() {\n"
				+ "				assertEquals(10L, 10L);\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_assertThat_shouldNotTransform() throws Exception {
		addDependency("org.hamcrest", "hamcrest-library", "1.3");
		addDependency("org.hamcrest", "hamcrest-core", "1.3");

		defaultFixture.addImport("org.junit.Assert.assertThat", true, false);
		defaultFixture.addImport("org.hamcrest.Matchers.equalToIgnoringCase", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = ""
				+ "		@Test\n"
				+ "		public void test() {\n"
				+ "			assertThat(\"value\", equalToIgnoringCase(\"value\"));\n"
				+ "		}\n"
				+ "	";

		assertNoChange(original);
	}
}