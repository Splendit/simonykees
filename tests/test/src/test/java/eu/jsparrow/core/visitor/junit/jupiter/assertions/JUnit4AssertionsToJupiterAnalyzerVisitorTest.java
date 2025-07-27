package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class JUnit4AssertionsToJupiterAnalyzerVisitorTest
		extends AbstractReplaceJUnit4WithJupiterASTVisitorTest {

	private static final String ORG_JUNIT_ASSERT = "org.junit.Assert";

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

	@ParameterizedTest
	@ValueSource(strings = { "@Ignore", "@Ignore()", "@Ignore(\"This test is ignored.\")" })
	public void visit_testWithJUnit4IgnoreAnnotation_shouldNotTransform(String ignoreAnnotation)
			throws Exception {
		defaultFixture.addImport(ORG_JUNIT_ASSERT);
		defaultFixture.addImport("org.junit.Ignore");
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	" + ignoreAnnotation + "\n" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assert.assertEquals(new Object[] {}, new Object[] {});\n" +
				"	}";
		assertNoChange(original);
	}

	/**
	 * Counterpart to
	 * {@link #visit_testWithJUnit4IgnoreAnnotation_shouldNotTransform(String)}
	 */
	@ParameterizedTest
	@ValueSource(strings = { "@Disabled", "@Disabled()", "@Disabled(\"This test is disabled.\")" })
	public void visit_testWithJUnitJupiterDisabledAnnotation_shouldTransform(String disableAnnotation)
			throws Exception {
		defaultFixture.addImport(ORG_JUNIT_ASSERT);
		defaultFixture.addImport(org.junit.jupiter.api.Disabled.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		String original = "" +
				"	" + disableAnnotation + "\n" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assert.assertEquals(new Object[] {}, new Object[] {});\n" +
				"	}";

		String expected = "" +
				"	" + disableAnnotation + "\n" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assertArrayEquals(new Object[]{},new Object[]{});\n" +
				"	}";
		List<String> expectedImports = Arrays.asList("import org.junit.Assert;",
				"import org.junit.jupiter.api.Disabled;",
				"import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertArrayEquals;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_testWithNonJUnitAnnotation_shouldTransform() throws Exception {
		defaultFixture.addImport(ORG_JUNIT_ASSERT);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("java.lang.annotation", false, true);

		String nonJUnitAnnotationDeclaration = "" +
				"	@Retention(RetentionPolicy.RUNTIME)\n" +
				"	@Target({ ElementType.METHOD })\n" +
				"	public @interface NonJUnitAnnotation {\n" +
				"	}";

		String original = "" +
				"	@NonJUnitAnnotation\n" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assert.assertEquals(new Object[] {}, new Object[] {});\n" +
				"	}\n\n" +
				nonJUnitAnnotationDeclaration;

		String expected = "" +
				"	@NonJUnitAnnotation\n" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assertArrayEquals(new Object[]{},new Object[]{});\n" +
				"	}\n\n" +
				nonJUnitAnnotationDeclaration;

		List<String> expectedImports = Arrays.asList(
				"import java.lang.annotation.*;",
				"import org.junit.Assert;",
				"import org.junit.jupiter.api.Test;",
				"import static org.junit.jupiter.api.Assertions.assertArrayEquals;");
		assertChange(original, expected, expectedImports);
	}
}
