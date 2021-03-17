package eu.jsparrow.core.visitor.junit.jupiter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReplaceJUnit4AnnotationsWithJupiterASTVisitorNegativeTest extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		setDefaultVisitor(new ReplaceJUnit4AnnotationsWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_JUnit4TestAnnotationWithTimeOut_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());

		String original = "" +
				"	@Test(timeout=1000L)\n" +
				"	public void test3() {\n" +
				"	}";

		assertNoChange(original);

	}

	@Test
	public void visit_BeforeClassNotJUnit4TestAnnotation_shouldNotTransform() throws Exception {
		defaultFixture.addImport(java.lang.annotation.Retention.class.getName());
		defaultFixture.addImport(java.lang.annotation.RetentionPolicy.class.getName());
		defaultFixture.addImport(java.lang.annotation.Target.class.getName());
		defaultFixture.addImport(java.lang.annotation.ElementType.class.getName());

		String original = "" +
				"\n" +
				"	@BeforeClass\n" +
				"	public void beforeAll() {\n" +
				"	}\n" +
				"\n" +
				"	@Retention(RetentionPolicy.RUNTIME)\n"
				+ "	@Target({ElementType.METHOD})\n"
				+ "	@interface BeforeClass {\n"
				+ "		\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_SingleMemberAnnotationRunWith_shouldNotTransform() throws Exception {
		String original = "" +
				"	@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)\n" +
				"	class TestRunningWithParameterized {\n" +
				"		\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_TestRuleAsField_shouldNotTransform() throws Exception {
		String original = "" +
				"	org.junit.rules.TestRule testRule;\n"
				+ "\n"
				+ "	@BeforeClass\n"
				+ "	public void beforeClass() {\n"
				+ "	}";
		assertNoChange(original);

	}
}
