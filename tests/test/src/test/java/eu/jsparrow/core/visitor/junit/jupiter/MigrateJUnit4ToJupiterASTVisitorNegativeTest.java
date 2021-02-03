package eu.jsparrow.core.visitor.junit.jupiter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

	/**
	 * Expected to fail as soon as the {@link org.junit.Assert} - class will be
	 * supported.
	 */
	@Test
	public void visit_JUnit4AssertImportedExplicitly_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.BeforeClass.class.getName());

		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeAll() {\n" +
				"		Assert.assertEquals(\"1\", \"1\");" +
				"	}";

		assertNoChange(original);
	}

	/**
	 * Expected to fail as soon as both the {@link org.junit.Assert} - class and
	 * the static import of its methods will be supported.
	 */
	@Test
	public void visit_JUnit4AssertEqualsStaticImport_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.BeforeClass.class.getName());

		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeAll() {\n" +
				"		assertEquals(\"1\", \"1\");" +
				"	}";

		assertNoChange(original);
	}

	/**
	 * Expected to fail as soon as both the {@link org.junit.Assert} - class and
	 * the static on-demand import of its methods will be supported.
	 */
	@Test
	public void visit_JUnit4AssertEqualsStaticImportOnDemand_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert", true, true);
		defaultFixture.addImport(org.junit.BeforeClass.class.getName());

		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeAll() throws Exception {\n" +
				"		assertEquals(\"1\", \"1\");\n" +
				"	}";

		assertNoChange(original);
	}

	@SuppressWarnings("restriction")
	@ParameterizedTest
	@ValueSource(classes = {
			junit.extensions.ActiveTestSuite.class,
			junit.framework.TestResult.class,
			junit.runner.BaseTestRunner.class,
			junit.textui.TestRunner.class,
			org.junit.Assert.class,
			org.junit.experimental.categories.Category.class,
			org.junit.experimental.ParallelComputer.class,
			org.junit.function.ThrowingRunnable.class,
			org.junit.internal.TextListener.class,
			org.junit.internal.runners.JUnit38ClassRunner.class,
			org.junit.internal.builders.AllDefaultPossibilitiesBuilder.class,
			org.junit.matchers.JUnitMatchers.class,
			org.junit.rules.DisableOnDebug.class,
			org.junit.runner.Computer.class,
			org.junit.runners.AllTests.class,
			org.junit.validator.AnnotationsValidator.class
	})
	public void visit_unexpectedJUnitTypeReferences_shouldNotTransform(Class<?> clazz) throws Exception {

		defaultFixture.addImport(org.junit.BeforeClass.class.getName());

		String toStringInvocation = clazz.getName() + ".toString();";
		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeClass() {\n" +
				"	" + toStringInvocation +
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
