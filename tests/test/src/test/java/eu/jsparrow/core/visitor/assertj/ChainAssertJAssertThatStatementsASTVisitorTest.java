package eu.jsparrow.core.visitor.assertj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesJDTUnitFixture;

@SuppressWarnings("nls")
class ChainAssertJAssertThatStatementsASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	void setUpVisitor() throws Exception {
		addDependency("org.assertj", "assertj-core", "3.21.0");
		setDefaultVisitor(new ChainAssertJAssertThatStatementsASTVisitor());
	}

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	void visit_AssertThatListIsNotNullAndIsNotEmpty_shouldTransform() throws Exception {

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
	void visit_AssertThatOnThisField_shouldTransform() throws Exception {

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
	void visit_AssertThatOnSuperField_shouldTransform() throws Exception {

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
	void visit_AssertionsOnConstant_shouldTransform() throws Exception {

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
	void visit_AssertThatOnThis_shouldTransform() throws Exception {

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
	void visit_AssertThatOnNumberLiteral_shouldTransform() throws Exception {

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
	void visit_AssertThatOnCharatcerLiteral_shouldTransform() throws Exception {

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
	void visit_AssertThatOnStringLiteral_shouldTransform() throws Exception {

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
	void visit_AssertThatOnTypeLiteral_shouldTransform() throws Exception {

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
	void visit_AssertThatOnThisFieldAccessChain_shouldTransform() throws Exception {

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
	void visit_AssertThatOnSuperFieldAccessChain_shouldTransform() throws Exception {
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
	void visit_AssertThatWithStringAsssertions_shouldTransform(String assertion) throws Exception {
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
	void visit_AssertThatWithFileAsssertions_shouldTransform(String assertion) throws Exception {
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
	void visit_AssertThatWithListAsssertions_shouldTransform(String assertion) throws Exception {
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
	void visit_AssertThatWithConditionAssertions_shouldTransform(String assertion) throws Exception {
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

	@ParameterizedTest
	@ValueSource(strings = {
			"rejects(\"a\", \"b\", \"c\")",
			"accepts(\"s-1\", \"s-2\")"
	})
	void visit_PredicateAssertions_shouldTransform(String assertion) throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
		defaultFixture.addImport(java.util.function.Predicate.class.getName());

		String original = "" +
				"	Predicate<String> stringPredicate = s -> s.contains(\"-\");\n"
				+ "\n"
				+ "	public void assertThatWithPredicateAssertion() {\n"
				+ "		assertThat(stringPredicate).isNotNull();\n"
				+ "		assertThat(stringPredicate)." + assertion + ";\n"
				+ "	}";

		String expected = "" +
				"	Predicate<String> stringPredicate = s -> s.contains(\"-\");\n"
				+ "\n"
				+ "	public void assertThatWithPredicateAssertion() {\n"
				+ "		assertThat(stringPredicate).isNotNull()." + assertion + ";\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertThatCode_shouldTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThatCode", true, false);
		defaultFixture.addImport("org.assertj.core.api.ThrowableAssert.ThrowingCallable");

		String original = "" +
				"	ThrowingCallable throwingCallable = () -> {\n"
				+ "		throw new Exception(\"Exception!\");\n"
				+ "	};\n"
				+ "\n"
				+ "	public void assertThatCodeAssertion() {\n"
				+ "\n"
				+ "		assertThatCode(throwingCallable).isInstanceOf(Exception.class);\n"
				+ "		assertThatCode(throwingCallable).hasMessageContaining(\"Exception\");\n"
				+ "	}";

		String expected = "" +
				"	ThrowingCallable throwingCallable = () -> {\n"
				+ "		throw new Exception(\"Exception!\");\n"
				+ "	};\n"
				+ "\n"
				+ "	public void assertThatCodeAssertion() {\n"
				+ "\n"
				+ "		assertThatCode(throwingCallable).isInstanceOf(Exception.class).hasMessageContaining(\"Exception\");\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertThatThrownBy_shouldTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThatThrownBy", true, false);
		defaultFixture.addImport("org.assertj.core.api.ThrowableAssert.ThrowingCallable");

		String original = "" +
				"	ThrowingCallable throwingCallable = () -> {\n"
				+ "		throw new Exception(\"Exception!\");\n"
				+ "	};\n"
				+ "\n"
				+ "	public void assertThatThrownByAssertion() {\n"
				+ "		assertThatThrownBy(throwingCallable).isInstanceOf(Exception.class);\n"
				+ "		assertThatThrownBy(throwingCallable).hasMessageContaining(\"Exception\");\n"
				+ "	}";

		String expected = "" +
				"	ThrowingCallable throwingCallable = () -> {\n"
				+ "		throw new Exception(\"Exception!\");\n"
				+ "	};\n"
				+ "\n"
				+ "	public void assertThatThrownByAssertion() {\n"
				+ "		assertThatThrownBy(throwingCallable).isInstanceOf(Exception.class).hasMessageContaining(\"Exception\");\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertThatObject_shouldTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.Assertions.assertThatObject", true, false);

		String original = "" +
				"	Object o = new Object();\n"
				+ "\n"
				+ "	public void assertThatObjectAssertion() {\n"
				+ "		assertThatObject(o).isNotNull();\n"
				+ "		assertThatObject(o).isSameAs(o);\n"
				+ "	}";

		String expected = "" +
				"	Object o = new Object();\n"
				+ "\n"
				+ "	public void assertThatObjectAssertion() {\n"
				+ "		assertThatObject(o).isNotNull().isSameAs(o);\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertionsForClassTypes_shouldTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.AssertionsForClassTypes.assertThat", true, false);

		String original = "" +
				"	Integer xInteger = Integer.valueOf(0);\n"
				+ "	\n"
				+ "	public void assertThatAssertionsForClassTypes() {\n"
				+ "		assertThat(xInteger).isZero();\n"
				+ "		assertThat(xInteger).isNotEqualTo(1);\n"
				+ "	}";

		String expected = "" +
				"	Integer xInteger = Integer.valueOf(0);\n"
				+ "	\n"
				+ "	public void assertThatAssertionsForClassTypes() {\n"
				+ "		assertThat(xInteger).isZero().isNotEqualTo(1);\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertionsForInterfaceTypes_shouldTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.AssertionsForInterfaceTypes.assertThat", true, false);
		defaultFixture.addImport(java.util.Arrays.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());

		String original = "" +
				"	List<String> strings = Arrays.asList(\"s1\", \"s2\", \"s3\");\n"
				+ "\n"
				+ "	public void assertThatAssertionsForInterfaceTypes() {\n"
				+ "		assertThat(strings).isNotNull();\n"
				+ "		assertThat(strings).hasSize(3);\n"
				+ "	}";

		String expected = "" +
				"	List<String> strings = Arrays.asList(\"s1\", \"s2\", \"s3\");\n"
				+ "\n"
				+ "	public void assertThatAssertionsForInterfaceTypes() {\n"
				+ "		assertThat(strings).isNotNull().hasSize(3);\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	void visit_MoreThanTwoInvocationsIn1stChain_shouldTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.AssertionsForInterfaceTypes.assertThat", true, false);
		defaultFixture.addImport(java.util.Arrays.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		
		String original = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void testMoreThanTwoInvocationsIn1stChain() {\n"
				+ "		assertThat(stringList).isNotNull().hasSize(2);\n"
				+ "		assertThat(stringList).contains(\"s1\", \"s1\");\n"
				+ "	}";

		String expected = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void testMoreThanTwoInvocationsIn1stChain() {\n"
				+ "		assertThat(stringList).isNotNull().hasSize(2).contains(\"s1\", \"s1\");\n"
				+ "	}";

		assertChange(original, expected);
	}
	
	@Test
	void visit_MoreThanTwoInvocationsIn2ndChain_shouldTransform() throws Exception {
		defaultFixture.addImport("org.assertj.core.api.AssertionsForInterfaceTypes.assertThat", true, false);
		defaultFixture.addImport(java.util.Arrays.class.getName());
		defaultFixture.addImport(java.util.List.class.getName());
		
		String original = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void testMoreThanTwoInvocationsIn2ndChain() {\n"
				+ "		assertThat(stringList).isNotNull();\n"
				+ "		assertThat(stringList).hasSize(2).contains(\"s1\", \"s1\");\n"
				+ "	}";

		String expected = "" +
				"	List<String> stringList = Arrays.asList(\"s1\", \"s2\");\n"
				+ "\n"
				+ "	public void testMoreThanTwoInvocationsIn2ndChain() {\n"
				+ "		assertThat(stringList).isNotNull().hasSize(2).contains(\"s1\", \"s1\");\n"
				+ "	}";

		assertChange(original, expected);
	}

}