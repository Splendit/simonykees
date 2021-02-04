package eu.jsparrow.core.visitor.junit.jupiter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MigrateJUnit4ToJupiterASTVisitorNegativeTest extends AbstractMigrateJUnit4ToJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		setDefaultVisitor(new MigrateJUnit4ToJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	/**
	 * Expected to fail as soon as both import on demand and the
	 * {@link org.junit.Assert} - class will be supported.
	 */
	@Test
	public void visit_JUnit4AssertImportedOnDemand_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit", false, true);

		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeAll() {\n" +
				"		Assert.assertEquals(\"1\", \"1\");" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_JUnit4TestAnnotation_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.Test.class.getName());

		String original = "" +
				"\n" +
				"	@Test\n" +
				"	public void test1() {\n" +
				"	}\n" +
				"\n" +
				"	@Test()\n" +
				"	public void test2() {\n" +
				"	}\n" +
				"\n" +
				"	@Test(timeout=1000L)\n" +
				"	public void test3() {\n" +
				"	}";

		assertNoChange(original);

	}

	@Test
	public void visit_NotJUnit4TestAnnotation_shouldNotTransform() throws Exception {
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

	/*
	 * SIM-1873: expected to fail as soon import on demand will be supported
	 */
	@Test
	public void visit_IgnoreImportedOnDemand_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit", false, true);

		String original = "" +
				"	@Ignore\n" +
				"	public void test() {\n" +
				"	}";

		assertNoChange(original);
	}

	/*
	 * SIM-1873: expected to fail as soon qualified annotation names will be
	 * supported
	 */
	@Test
	public void visit_JUnit4TestAnnotationNotSimpleTypeName_shouldNotTransform() throws Exception {

		String original = "" +
				"\n" +
				"	@org.junit.BeforeClass\n" +
				"	public void beforeAll() {\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_SingleMemberAnnotationRunWith_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.runner.RunWith.class.getName());
		defaultFixture.addImport(org.junit.runners.Parameterized.class.getName());

		String original = "" +
				"	@RunWith(Parameterized.class)\n" +
				"	class TestRunningWithParameterized {\n" +
				"		\n" +
				"	}";
		assertNoChange(original);
	}
}
