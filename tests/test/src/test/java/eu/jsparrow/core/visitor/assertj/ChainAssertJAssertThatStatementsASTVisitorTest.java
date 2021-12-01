package eu.jsparrow.core.visitor.assertj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings("nls")
public class ChainAssertJAssertThatStatementsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("org.assertj", "assertj-core", "3.21.0");
		setDefaultVisitor(new ChainAssertJAssertThatStatementsASTVisitor());
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_AssertThatListIsNotNullAndIsNotEmpty_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void testIsNotNullIsNotEmpty() {\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList).isNotEmpty();\n"
				+ "	}";

		String expected = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void testIsNotNullIsNotEmpty() {\n"
				+ "		assertThat(stringList).isNotNull().isNotEmpty();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnThisField_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void assertNotNullAndNotEmpty() {\n"
				+ "		assertThat(this.stringList).isNotNull();\n"
				+ "		assertThat(this.stringList).isNotEmpty();\n"
				+ "	}";

		String expected = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void assertNotNullAndNotEmpty() {\n"
				+ "		assertThat(this.stringList).isNotNull().isNotEmpty();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnSuperField_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport(java.util.List.class.getName());
		defaultFixture.addImport(java.util.Arrays.class.getName());

		String original = "" +
				"	class SuperClass {\n"
				+ "		List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubClass extends SuperClass {\n"
				+ "		public void assertNotNullAndNotEmpty() {\n"
				+ "			assertThat(super.stringList).isNotNull();\n"
				+ "			assertThat(super.stringList).isNotEmpty();\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	class SuperClass {\n"
				+ "		List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubClass extends SuperClass {\n"
				+ "		public void assertNotNullAndNotEmpty() {\n"
				+ "			assertThat(super.stringList).isNotNull().isNotEmpty();\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertionsOnConstant_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	public void test() {\n"
				+ "		assertThat(java.lang.Integer.MAX_VALUE).isNotEqualTo(1000);\n"
				+ "		assertThat(java.lang.Integer.MAX_VALUE).isGreaterThan(1000);\n"
				+ "	}";

		String expected = "" +
				"	public void test() {\n"
				+ "		assertThat(java.lang.Integer.MAX_VALUE).isNotEqualTo(1000).isGreaterThan(1000);\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnThis_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	class ExampleClass {\n"
				+ "		void test() {\n"
				+ "			assertThat(this).isNotNull();\n"
				+ "			assertThat(this).isInstanceOf(ExampleClass.class);\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	class ExampleClass {\n"
				+ "		void test() {\n"
				+ "			assertThat(this).isNotNull().isInstanceOf(ExampleClass.class);\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnNumberLiteral_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	public void assertThatOnIntLiteral() {\n"
				+ "		assertThat(1000).isEqualTo(1000);\n"
				+ "		assertThat(1000).isGreaterThan(999);\n"
				+ "	}";

		String expected = "" +
				"	public void assertThatOnIntLiteral() {\n"
				+ "		assertThat(1000).isEqualTo(1000).isGreaterThan(999);\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnCharatcerLiteral_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	public void assertThatOnCharacterLiteral() {\n"
				+ "		assertThat('A').isEqualTo('A');\n"
				+ "		assertThat('A').isGreaterThan('@');\n"
				+ "	}";

		String expected = "" +
				"	public void assertThatOnCharacterLiteral() {\n"
				+ "		assertThat('A').isEqualTo('A').isGreaterThan('@');\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnStringLiteral_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);

		String original = "" +
				"	public void assertThatOnStringLiteral() {\n"
				+ "		assertThat(\"HelloWorld\").isNotNull();\n"
				+ "		assertThat(\"HelloWorld\").isNotEmpty();\n"
				+ "	}";

		String expected = "" +
				"	public void assertThatOnStringLiteral() {\n"
				+ "		assertThat(\"HelloWorld\").isNotNull().isNotEmpty();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnTypeLiteral_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);

		String original = "" +
				"	public void assertThatOnTypeLiteral() {\n"
				+ "		assertThat(String.class).isFinal();\n"
				+ "		assertThat(String.class).isNotInterface();\n"
				+ "	}";

		String expected = "" +
				"	public void assertThatOnTypeLiteral() {\n"
				+ "		assertThat(String.class).isFinal().isNotInterface();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnThisFieldAccessChain_shouldTransform() throws Exception {

		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);

		String original = "" +
				"	IntWrapper intWrapper = new IntWrapper();\n"
				+ "\n"
				+ "	public void assertThatOnThisFieldAccessChain() {\n"
				+ "\n"
				+ "		assertThat(this.intWrapper.intValue).isEqualTo(0);\n"
				+ "		assertThat(this.intWrapper.intValue).isGreaterThan(-1);\n"
				+ "	}\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		String expected = "" +
				"	IntWrapper intWrapper = new IntWrapper();\n"
				+ "\n"
				+ "	public void assertThatOnThisFieldAccessChain() {\n"
				+ "\n"
				+ "		assertThat(this.intWrapper.intValue).isEqualTo(0).isGreaterThan(-1);\n"
				+ "	}\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_AssertThatOnSuperFieldAccessChain_shouldTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat",
				true, false);

		String original = "" +
				"	class SuperClass {\n"
				+ "		IntWrapper intWrapper = new IntWrapper();\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubClass extends SuperClass {\n"
				+ "		public void assertThatOnSuperFieldAccessChain() {\n"
				+ "			assertThat(super.intWrapper.intValue).isEqualTo(0);\n"
				+ "			assertThat(super.intWrapper.intValue).isGreaterThan(-1);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		String expected = "" +
				"	class SuperClass {\n"
				+ "		IntWrapper intWrapper = new IntWrapper();\n"
				+ "	}\n"
				+ "\n"
				+ "	class SubClass extends SuperClass {\n"
				+ "		public void assertThatOnSuperFieldAccessChain() {\n"
				+ "			assertThat(super.intWrapper.intValue).isEqualTo(0).isGreaterThan(-1);\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	class IntWrapper {\n"
				+ "		int intValue;\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"hasSize(helloWorld.length())",
			"startsWith(\"Hello\")",
			"contains(\"llo W\")",
			"endsWith(\"World!\")",
			"doesNotContain(\"?\")",
			"matches(\"Hello.*\")"
	})
	public void visit_AssertThatWithStringAsssertions_shouldTransform(String assertion) throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		String original = "" +
				"	private String helloWorld = \"Hello World!\";\n"
				+ "\n"
				+ "	public void assertThatWithStringAssertion() {\n"
				+ "		assertThat(helloWorld).isNotNull();\n"
				+ "		assertThat(helloWorld)." + assertion + ";\n"
				+ "	}";

		String expected = "" +
				"	private String helloWorld = \"Hello World!\";\n"
				+ "\n"
				+ "	public void assertThatWithStringAssertion() {\n"
				+ "		assertThat(helloWorld).isNotNull()." + assertion + ";\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"exists()",
			"satisfies(File::isFile)",
			"hasFileName(\"pom.xml\")",
			"canRead()"
	})
	public void visit_AssertThatWithFileAsssertions_shouldTransform(String assertion) throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		defaultFixture.addImport(java.io.File.class.getName());

		String original = "" +
				"	private File file = new File(\"pom.xml\");\n"
				+ "\n"
				+ "	public void assertThatWithStringAssertion() {\n"
				+ "		assertThat(file).isNotNull();\n"
				+ "		assertThat(file)." + assertion + ";\n"
				+ "	}";

		String expected = "" +
				"	private File file = new File(\"pom.xml\");\n"
				+ "\n"
				+ "	public void assertThatWithStringAssertion() {\n"
				+ "		assertThat(file).isNotNull()." + assertion + ";\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"allSatisfy(s -> assertThat(s).startsWith(\"s\"))",
			"anySatisfy(s -> assertThat(s).isEqualTo(\"s1\"))",
			"noneSatisfy(s -> assertThat(s).isEqualTo(\"s3\"))",
			"allMatch(s -> s.startsWith(\"s\"))",
			"anyMatch(s -> s.equals(\"s1\"))",
			"noneMatch(s -> s.equals(\"s3\"))"
	})
	public void visit_AssertThatWithListAsssertions_shouldTransform(String assertion) throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);

		defaultFixture.addImport(java.util.Arrays.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());

		String original = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void assertThatWithListAssertion() {\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList)." + assertion + ";\n"
				+ "	}";

		String expected = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void assertThatWithListAssertion() {\n"
				+ "		assertThat(stringList).isNotNull()." + assertion + ";\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"are(startsWithXAndDash)",
			"have(startsWithXAndDash)"
	})
	public void visit_AssertThatWithConditionAssertions_shouldTransform(String assertion) throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport("org.assertj.core.api.Condition");

		String original = "" +
				"	Iterable<String> strings = Arrays.asList(\"x-1\", \"x-2\", \"x-3\");\n"
				+ "	Condition<String> startsWithXAndDash = new Condition<>(s -> s.startsWith(\"x-\"), \"starts with x and dash\");\n"
				+ "\n"
				+ "	public void assertThatWithConditionAssertion() {\n"
				+ "		assertThat(strings).isNotNull();\n"
				+ "		assertThat(strings)." + assertion + ";\n"
				+ "	}";

		String expected = "" +
				"	Iterable<String> strings = Arrays.asList(\"x-1\", \"x-2\", \"x-3\");\n"
				+ "	Condition<String> startsWithXAndDash = new Condition<>(s -> s.startsWith(\"x-\"), \"starts with x and dash\");\n"
				+ "\n"
				+ "	public void assertThatWithConditionAssertion() {\n"
				+ "		assertThat(strings).isNotNull()." + assertion + ";\n"
				+ "	}";

		assertChange(original, expected);
	}
}