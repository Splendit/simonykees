package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
class UseDedicatedAssertJAssertionsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		setVisitor(new UseDedicatedAssertJAssertionsASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	public static Stream<Arguments> stringMethodIsTrue() throws Exception {
		return Stream.of(
				Arguments.of("equals(\"str-1\")", "isEqualTo(\"str-1\")"),
				Arguments.of("equalsIgnoreCase(\"STR-1\")", "isEqualToIgnoringCase(\"STR-1\")"),
				Arguments.of("startsWith(\"str\")", "startsWith(\"str\")"),
				Arguments.of("contains(\"-\")", "contains(\"-\")"),
				Arguments.of("endsWith(\"1\")", "endsWith(\"1\")"),
				Arguments.of("isEmpty()", "isEmpty()"),
				Arguments.of("isBlank()", "isBlank()"));
	}

	public static Stream<Arguments> stringMethodIsFalse() throws Exception {
		return Stream.of(
				Arguments.of("equals(\"str-1\")", "isNotEqualTo(\"str-1\")"),
				Arguments.of("equalsIgnoreCase(\"STR-1\")", "isNotEqualToIgnoringCase(\"STR-1\")"),
				Arguments.of("startsWith(\"str\")", "doesNotStartWith(\"str\")"),
				Arguments.of("contains(\"-\")", "doesNotContain(\"-\")"),
				Arguments.of("endsWith(\"1\")", "doesNotEndWith(\"1\")"),
				Arguments.of("isEmpty()", "isNotEmpty()"),
				Arguments.of("isBlank()", "isNotBlank()"));
	}

	public static Stream<Arguments> listMethodIsTrue() throws Exception {
		return Stream.of(
				Arguments.of("equals(stringList)", "isEqualTo(stringList)"),
				Arguments.of("contains(\"str-1\")", "contains(\"str-1\")"),
				Arguments.of("containsAll(Arrays.asList(\"str-1\", \"str-2\"))",
						"containsAll(Arrays.asList(\"str-1\", \"str-2\"))"),
				Arguments.of("isEmpty()", "isEmpty()"));
	}

	public static Stream<Arguments> listMethodIsFalse() throws Exception {
		return Stream.of(
				Arguments.of("equals(stringList)", "isNotEqualTo(stringList)"),
				Arguments.of("contains(\"str-1\")", "doesNotContain(\"str-1\")"),
				Arguments.of("isEmpty()", "isNotEmpty()"));
	}

	public static Stream<Arguments> mapMethodIsTrue() throws Exception {
		return Stream.of(
				Arguments.of("equals(map)", "isEqualTo(map)"),
				Arguments.of("containsKey(\"key-1\")", "containsKey(\"key-1\")"),
				Arguments.of("containsValue(\"value-1\")",
						"containsValue(\"value-1\")"),
				Arguments.of("isEmpty()", "isEmpty()"));
	}

	public static Stream<Arguments> mapMethodIsFalse() throws Exception {
		return Stream.of(
				Arguments.of("equals(new HashMap<>())", "isNotEqualTo(new HashMap<>())"),
				Arguments.of("containsKey(\"key-1\")",
						"doesNotContainKey(\"key-1\")"),
				Arguments.of("containsValue(\"value-1\")",
						"doesNotContainValue(\"value-1\")"),
				Arguments.of("isEmpty()", "isNotEmpty()"));
	}

	@ParameterizedTest
	@MethodSource("stringMethodIsTrue")
	void visit_AssertThatStringMethodIsTrue_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {

		String original = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1.%s).isTrue();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@MethodSource("stringMethodIsFalse")
	void visit_AssertThatStringMethodIsFalse_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {

		String original = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1.%s).isFalse();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@MethodSource("stringMethodIsFalse")
	void visit_AssertThatNegatedStringMethodIsTrue_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {

		String original = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(!s1.%s).isTrue();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@MethodSource("stringMethodIsTrue")
	void visit_AssertThatNegatedStringMethodIsFalse_shouldTransform(String originalInvocation,
			String expectedInvocation)
			throws Exception {

		String original = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(!s1.%s).isFalse();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		String s1 = \"str-1\";\n" +
						"		assertThat(s1).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@MethodSource("listMethodIsTrue")
	void visit_AssertThatListMethodIsTrue_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = String.format(
				"" +
						"		List<String> stringList = Arrays.asList(\"str-1\", \"str-2\", \"str-3\");\n" +
						"		assertThat(stringList.%s).isTrue();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		List<String> stringList = Arrays.asList(\"str-1\", \"str-2\", \"str-3\");\n" +
						"		assertThat(stringList).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@MethodSource("listMethodIsFalse")
	void visit_AssertThatListMethodIsFalse_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = String.format(
				"" +
						"		List<String> stringList = Arrays.asList(\"str-1\", \"str-2\", \"str-3\");\n" +
						"		assertThat(stringList.%s).isFalse();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		List<String> stringList = Arrays.asList(\"str-1\", \"str-2\", \"str-3\");\n" +
						"		assertThat(stringList).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}

	@Test
	void visit_AssertThatListContainsAllIsFalse_shouldNotTransform()
			throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = "" +
				"		List<String> stringList = Arrays.asList(\"str-1\", \"str-2\");\n"
				+ "		assertThat(stringList.containsAll(Arrays.asList(\"str-1\", \"str-3\"))).isFalse();";

		assertNoChange(original);
	}

	@ParameterizedTest
	@MethodSource("mapMethodIsTrue")
	void visit_AssertThatMapMethodIsTrue_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {

		fixture.addImport(java.util.HashMap.class.getName());
		fixture.addImport(java.util.Map.class.getName());

		String original = String.format(
				"" +
						"		Map<String, String> map = new HashMap<>();\n"
						+ "		map.put(\"key-1\", \"value-1\");\n"
						+ "		assertThat(map.%s).isTrue();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		Map<String, String> map = new HashMap<>();\n"
						+ "		map.put(\"key-1\", \"value-1\");\n"
						+ "		assertThat(map).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@MethodSource("mapMethodIsFalse")
	void visit_AssertThatMapMethodIsFalse_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {

		fixture.addImport(java.util.HashMap.class.getName());
		fixture.addImport(java.util.Map.class.getName());

		String original = String.format(
				"" +
						"		Map<String, String> map = new HashMap<>();\n"
						+ "		map.put(\"key-1\", \"value-1\");\n"
						+ "		assertThat(map.%s).isFalse();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		Map<String, String> map = new HashMap<>();\n"
						+ "		map.put(\"key-1\", \"value-1\");\n"
						+ "		assertThat(map).%s;",
				expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> pathMethodIsTrue() throws Exception {
		return Stream.of(
				Arguments.of("equals(path)", "isEqualTo(path)"),
				Arguments.of("isAbsolute()", "isAbsolute()"),
				Arguments.of("startsWith(Path.of(\"/home/gregor/\"))", "startsWith(Path.of(\"/home/gregor/\"))"),
				Arguments.of("endsWith(Path.of(\"pom.xml\"))", "endsWith(Path.of(\"pom.xml\"))"));
	}

	@ParameterizedTest
	@MethodSource("pathMethodIsTrue")
	void visit_AssertThatPathMethodIsTrue_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {

		fixture.addImport(java.nio.file.Path.class.getName());
		String pathVariableDeclaration = "Path path = Path.of(\"/home/gregor/opensource/eclipse-plugin-tests/workspace/simple-test/pom.xml\");";

		String original = String.format("" +
				"		%s\n" +
				"		assertThat(path.%s).isTrue();",
				pathVariableDeclaration, originalInvocation);

		String expected = String.format("" +
				"		%s\n" +
				"		assertThat(path).%s;",
				pathVariableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"startsWith(\"/home/gregor/\")",
			"endsWith(\"pom.xml\")"
	})
	void visit_AssertThatPathMethodIsTrue_shouldNotTransform(String originalInvocation)
			throws Exception {

		fixture.addImport(java.nio.file.Path.class.getName());
		String pathVariableDeclaration = "Path path = Path.of(\"/home/gregor/opensource/eclipse-plugin-tests/workspace/simple-test/pom.xml\");";

		String original = String.format("" +
				"		%s\n" +
				"		assertThat(path.%s).isTrue();",
				pathVariableDeclaration, originalInvocation);

		assertNoChange(original);
	}
}