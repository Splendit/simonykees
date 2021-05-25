package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.junit.jupiter.AbstractReplaceJUnit4WithJupiterASTVisitorTest;

public class ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitorTest
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
	public void visit_unqualifiedAssumeThat_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeThat", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(new Object(), notNullValue());\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");
		assertChange(original, original, expectedImports);
	}

	@Test
	public void visit_AssumeThatWithMessage_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeThat", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(\"Assumed that {new Object} is not null.\", new Object(), notNullValue());\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");
		assertChange(original, original, expectedImports);
	}

	@Test
	public void visit_qualifiedAssumeThat_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assume.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		Assume.assumeThat(new Object(), notNullValue());\n" +
				"	}";
		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(new Object(),notNullValue());\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.Assume;",
				"import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_needingMatcherAssumeQualifier_shouldTransform() throws Exception {
		defaultFixture.addImport(org.junit.Assume.class.getName());
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	void assumeThat() {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		Assume.assumeThat(\"value\", notNullValue());\n"
				+ "	}";

		String expected = "" +
				"	void assumeThat() {\n"
				+ "	}\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		MatcherAssume.assumeThat(\"value\", notNullValue());\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.hamcrest.junit.MatcherAssume;",
				"import org.junit.Assume;",
				"import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_unqualifiedAssumeNoException_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNoException", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());
		defaultFixture.addImport("org.hamcrest.CoreMatchers.notNullValue", true, false);
		String original = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeNoException(null);\n" +
				"	}";
		String expected = "" +
				"	@Test\n" +
				"	void test() {\n" +
				"		assumeThat(null,nullValue());\n" +
				"	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.CoreMatchers.nullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");
		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithArrayCreationContainingNull_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull(new Object[] { null });\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(asList(new Object[]{null}),everyItem(notNullValue()));\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static java.util.Arrays.asList;",
				"import static org.hamcrest.CoreMatchers.everyItem;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithNullableObjectArray_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	Object[] objects;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull(objects);\n"
				+ "	}";
		String expected = "" +
				"	Object[] objects;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(objects,notNullValue());\n"
				+ "		assumeThat(asList(objects),everyItem(notNullValue()));\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static java.util.Arrays.asList;",
				"import static org.hamcrest.CoreMatchers.everyItem;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithNullableIntArray_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	int[] intArry;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull(intArry);\n"
				+ "	}";
		String expected = "" +
				"	int[] intArry;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(intArry,notNullValue());\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithNullLiteral_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull(null);\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(null,notNullValue());\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithObjectArrayAsObject_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	Object[] objects;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		Object o = objects;\n"
				+ "		assumeNotNull(o);\n"
				+ "	}";
		String expected = "" +
				"	Object[] objects;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		Object o = objects;\n"
				+ "		assumeThat(o,notNullValue());\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithoutArguments_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull();\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(asList(),everyItem(notNullValue()));\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static java.util.Arrays.asList;", "import static org.hamcrest.CoreMatchers.everyItem;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithSingleObject_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	Object o;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull(o);\n"
				+ "	}";
		String expected = "" +
				"	Object o;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(o,notNullValue());\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithStringArrayCreation_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull(new String[] { \"A\", \"B\" });\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(asList(new String[]{\"A\",\"B\"}),everyItem(notNullValue()));\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static java.util.Arrays.asList;",
				"import static org.hamcrest.CoreMatchers.everyItem;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithTwoNullLiterals_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull(null, null);\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(asList(null,null),everyItem(notNullValue()));\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static java.util.Arrays.asList;",
				"import static org.hamcrest.CoreMatchers.everyItem;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithTwoObjectArrays_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	Object[] objects1;\n"
				+ "	Object[] objects2;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull(objects1, objects2);\n"
				+ "	}";
		String expected = "" +
				"	Object[] objects1;\n"
				+ "	Object[] objects2;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeThat(asList(objects1,objects2),everyItem(notNullValue()));\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static java.util.Arrays.asList;",
				"import static org.hamcrest.CoreMatchers.everyItem;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_ConditionalAssumeNotNull_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume.assumeNotNull", true, false);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		if (true)\n"
				+ "			assumeNotNull(new Object[] { \"\", \"\" });\n"
				+ "	}";
		String expected = "" +
				"	@Test\n"
				+ "	public void test() {\n"
				+ "		if (true)\n"
				+ "			assumeThat(asList(new Object[]{\"\",\"\"}),everyItem(notNullValue()));\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.junit.jupiter.api.Test;",
				"import static java.util.Arrays.asList;",
				"import static org.hamcrest.CoreMatchers.everyItem;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_AssumeNotNullWithNullableArrayAndImportOnDemand_shouldTransform() throws Exception {
		defaultFixture.addImport("org.junit.Assume", true, true);
		defaultFixture.addImport(org.junit.jupiter.api.Test.class.getName());

		String original = "" +
				"	Object[] objects;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		assumeNotNull(objects);\n"
				+ "	}";
		String expected = "" +
				"	Object[] objects;\n"
				+ "\n"
				+ "	@Test\n"
				+ "	public void test() {\n"
				+ "		MatcherAssume.assumeThat(objects,notNullValue());\n"
				+ "		MatcherAssume.assumeThat(asList(objects),everyItem(notNullValue()));\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import org.hamcrest.junit.MatcherAssume;",
				"import org.junit.jupiter.api.Test;",
				"import static java.util.Arrays.asList;",
				"import static org.hamcrest.CoreMatchers.everyItem;",
				"import static org.hamcrest.CoreMatchers.notNullValue;",
				"import static org.junit.Assume.*;");

		assertChange(original, expected, expectedImports);
	}

	@Test
	public void visit_TestJUnit4AssumeThatNotInJUnitTest_shouldTransform() throws Exception {
		defaultFixture.addImport("org.hamcrest.Matchers.notNullValue", true, false);
		defaultFixture.addImport("org.junit.Assume.assumeThat", true, false);

		String original = "" +
				"	public void test() {\n"
				+ "		assumeThat(\"value\", notNullValue());\n"
				+ "	}";

		List<String> expectedImports = Arrays.asList("import static org.hamcrest.Matchers.notNullValue;",
				"import static org.hamcrest.junit.MatcherAssume.assumeThat;");
		assertChange(original, original, expectedImports);
	}
}