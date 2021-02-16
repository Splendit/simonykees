package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ReplaceJUnit4AnnotationsWithJupiterASTVisitorImportsTest extends AbstractReplaceJUnit4AnnotationsWithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		setDefaultVisitor(new ReplaceJUnit4AnnotationsWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@SuppressWarnings("restriction")
	@ParameterizedTest
	@ValueSource(classes = {
			junit.extensions.ActiveTestSuite.class,
			junit.framework.TestResult.class,
			junit.runner.BaseTestRunner.class,
			junit.textui.TestRunner.class,
			org.junit.ClassRule.class,
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
	public void visit_unexpectedJUnitTypeImports_shouldNotTransform(Class<?> clazz) throws Exception {

		defaultFixture.addImport(clazz.getName());

		String original = "" +
				"	public void test() {\n" +
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
	public void visit_unexpectedJUnitImportsOnDemand_shouldNotTransform(Class<?> clazz) throws Exception {

		defaultFixture.addImport(clazz.getPackage()
			.getName(), false, true);

		String original = "" +
				"	public void test() {\n" +
				"	}";

		assertNoChange(original);
	}
	
	@Test
	public void visit_UnusedBeforeClassImport_shouldTransform() throws Exception {

		defaultFixture.addImport(org.junit.BeforeClass.class.getName());

		String original = "" +
				"	public void test() {\n" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList();
		
		assertChange(original, original, importsToStringExpected);
	}

	@Test
	public void visit_ImplicitBeforeClassImport_shouldTransform() throws Exception {

		defaultFixture.addImport("org.junit", false, true);

		String original = "" +
				"	@BeforeClass\n" +
				"	public void beforeClass() {\n" +
				"	}";

		String expected = "" +
				"	@BeforeAll\n" +
				"	public void beforeClass() {\n" +
				"	}";

		List<String> importsToStringExpected = Arrays.asList(
				"import org.junit.*;",
				"import org.junit.jupiter.api.BeforeAll;");

		assertChange(original, expected, importsToStringExpected);
	}
}
