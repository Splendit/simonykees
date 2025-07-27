package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ReplaceJUnit4AnnotationsWithJupiterASTVisitorImportsTest extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "4.13");
		setDefaultVisitor(new ReplaceJUnit4AnnotationsWithJupiterASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"junit.extensions.ActiveTestSuite",
			"junit.framework.TestResult",
			"junit.runner.BaseTestRunner",
			"junit.textui.TestRunner",
			"org.junit.ClassRule",
			"org.junit.experimental.categories.Category",
			"org.junit.experimental.ParallelComputer",
			"org.junit.function.ThrowingRunnable",
			"org.junit.internal.TextListener",
			"org.junit.internal.runners.JUnit38ClassRunner",
			"org.junit.internal.builders.AllDefaultPossibilitiesBuilder",
			"org.junit.matchers.JUnitMatchers",
			"org.junit.rules.DisableOnDebug",
			"org.junit.runner.Computer",
			"org.junit.runners.AllTests",
			"org.junit.validator.AnnotationsValidator"
	})
	public void visit_unexpectedJUnitTypeImports_shouldNotTransform(String className) throws Exception {

		defaultFixture.addImport(className);

		String original = "" +
				"	public void test() {\n" +
				"	}";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"junit.extensions",
			"junit.framework",
			"junit.runner",
			"junit.textui",
			"org.junit.experimental.categories",
			"org.junit.experimental",
			"org.junit.function",
			"org.junit.internal",
			"org.junit.internal.runners",
			"org.junit.internal.builders",
			"org.junit.matchers",
			"org.junit.rules",
			"org.junit.runner",
			"org.junit.runners",
			"org.junit.validator"
	})
	public void visit_unexpectedJUnitImportsOnDemand_shouldNotTransform(String packageName) throws Exception {

		defaultFixture.addImport(packageName, false, true);

		String original = "" +
				"	public void test() {\n" +
				"	}";

		assertNoChange(original);
	}
	
	@Test
	public void visit_UnusedBeforeClassImport_shouldTransform() throws Exception {

		defaultFixture.addImport("org.junit.BeforeClass");

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
