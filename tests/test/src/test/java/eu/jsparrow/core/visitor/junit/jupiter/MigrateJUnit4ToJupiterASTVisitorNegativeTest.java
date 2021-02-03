package eu.jsparrow.core.visitor.junit.jupiter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

class MigrateJUnit4ToJupiterASTVisitorNegativeTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		setDefaultVisitor(new MigrateJUnit4ToJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_JUnit4AssertImportedOnDemand_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit", false, true);

		String original = "" +
				"	@Test\n" +
				"	public void test() {\n" +
				"		Assert.assertEquals(\"1\", \"1\");" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_JUnit4AssertImportedExplicitly_shouldNotTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assert.class.getName());
		defaultFixture.addImport(org.junit.Test.class.getName());

		String original = "" +
				"	@Test\n" +
				"	public void test() {\n" +
				"		Assert.assertEquals(\"1\", \"1\");" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_JUnit4AssertEqualsStaticImport_shouldNotTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assert.assertEquals", true, false);
		defaultFixture.addImport(org.junit.Test.class.getName());

		String original = "" +
				"	@Test\n" +
				"	public void test() {\n" +
				"		assertEquals(\"1\", \"1\");" +
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

		defaultFixture.addImport(org.junit.Test.class.getName());

		String toStringInvocation = clazz.getName() + ".toString();";
		String original = "" +
				"	@Test\n" +
				"	public void test() {\n" +
				"	" + toStringInvocation +
				"	}";

		assertNoChange(original);

	}
}
