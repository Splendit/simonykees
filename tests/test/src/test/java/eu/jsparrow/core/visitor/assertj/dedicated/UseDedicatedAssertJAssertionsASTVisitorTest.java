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

	private static final String IS_TRUE = "isTrue()";
	private static final String IS_FALSE = "isFalse()";

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

	@Test
	void visit_AssertthatIterableEqualsIsTrue_shouldTransform()
			throws Exception {

		fixture.addImport(java.util.Arrays.class.getName());
		String original = "" +
				"		Iterable<String> iterable = Arrays.asList(\"str-1\", \"str-2\");\n"
				+ "		assertThat(iterable.equals(iterable)).isTrue();";
		String expected = "" +
				"		Iterable<String> iterable = Arrays.asList(\"str-1\", \"str-2\");\n"
				+ "		assertThat(iterable).isEqualTo(iterable);";

		assertChange(original, expected);
	}

	@Test
	void visit_AssertthatIterableEqualsIsFalse_shouldTransform()
			throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		String original = "" +
				"		Iterable<String> iterable = Arrays.asList(\"str-1\", \"str-2\");\n"
				+ "		assertThat(iterable.equals(Arrays.asList())).isFalse();";
		String expected = "" +
				"		Iterable<String> iterable = Arrays.asList(\"str-1\", \"str-2\");\n"
				+ "		assertThat(iterable).isNotEqualTo(Arrays.asList());";

		assertChange(original, expected);
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
	@MethodSource("mapMethodIsTrue")
	void visit_AssertThatHashMapMethodIsTrue_shouldTransform(String originalInvocation, String expectedInvocation)
			throws Exception {

		fixture.addImport(java.util.HashMap.class.getName());
		fixture.addImport(java.util.Map.class.getName());

		String original = String.format(
				"" +
						"		HashMap<String, String> map = new HashMap<>();\n"
						+ "		map.put(\"key-1\", \"value-1\");\n"
						+ "		assertThat(map.%s).isTrue();",
				originalInvocation);

		String expected = String.format(
				"" +
						"		HashMap<String, String> map = new HashMap<>();\n"
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

	public static Stream<Arguments> assertionsOnOptionalMethods() throws Exception {
		return Stream.of(
				Arguments.of("equals(optional)", "isEqualTo(optional)", IS_TRUE),
				Arguments.of("isPresent()", "isPresent()", IS_TRUE),
				Arguments.of("isEmpty()", "isEmpty()", IS_TRUE),
				Arguments.of("equals(optional)", "isNotEqualTo(optional)", IS_FALSE),
				Arguments.of("isPresent()", "isEmpty()", IS_FALSE),
				Arguments.of("isEmpty()", "isPresent()", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnOptionalMethods")
	void visit_AssertionsWithOptionalMethods_shouldTransform(String originalInvocation, String expectedInvocation,
			String booleanAssertion)
			throws Exception {

		fixture.addImport(java.util.Optional.class.getName());

		String variableDeclaration = "Optional<String> optional = Optional.of(\"Hello World!\");";
		String original = String.format(
				"" +
						"		%s\n" +
						"		assertThat(optional.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		assertThat(optional).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnDateMethods() throws Exception {
		return Stream.of(
				Arguments.of("equals(new Date(currentTimeMillis))", "isEqualTo(new Date(currentTimeMillis))", IS_TRUE),
				Arguments.of("before(new Date(currentTimeMillis + 1000))",
						"isBefore(new Date(currentTimeMillis + 1000))", IS_TRUE),
				Arguments.of("after(new Date(currentTimeMillis - 1000))", "isAfter(new Date(currentTimeMillis - 1000))",
						IS_TRUE),
				Arguments.of("equals(new Date(currentTimeMillis + 1000))",
						"isNotEqualTo(new Date(currentTimeMillis + 1000))", IS_FALSE),
				Arguments.of("before(new Date(currentTimeMillis - 1000))",
						"isAfterOrEqualTo(new Date(currentTimeMillis - 1000))", IS_FALSE),
				Arguments.of("after(new Date(currentTimeMillis + 1000))",
						"isBeforeOrEqualTo(new Date(currentTimeMillis + 1000))", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnDateMethods")
	void visit_AssertionsWithDateMethods_shouldTransform(String originalInvocation, String expectedInvocation,
			String booleanAssertion)
			throws Exception {

		fixture.addImport(java.util.Date.class.getName());

		String variableDeclaration = ""
				+ "		long currentTimeMillis = System.currentTimeMillis();\n"
				+ "		Date date = new Date(currentTimeMillis);";

		String original = String.format(
				"" +
						"		%s\n" +
						"		assertThat(date.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		assertThat(date).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnLocalDateMethods() throws Exception {
		return Stream.of(
				Arguments.of("equals(LocalDate.of(2020, 11, 1))", "isEqualTo(LocalDate.of(2020, 11, 1))", IS_TRUE),
				Arguments.of("isBefore(LocalDate.of(2020, 12, 1))", "isBefore(LocalDate.of(2020, 12, 1))", IS_TRUE),
				Arguments.of("isAfter(LocalDate.of(2020, 10, 1))", "isAfter(LocalDate.of(2020, 10, 1))", IS_TRUE),
				Arguments.of("equals(LocalDate.of(2020, 10, 1))", "isNotEqualTo(LocalDate.of(2020, 10, 1))", IS_FALSE),
				Arguments.of("isBefore(LocalDate.of(2020, 10, 1))", "isAfterOrEqualTo(LocalDate.of(2020, 10, 1))",
						IS_FALSE),
				Arguments.of("isAfter(LocalDate.of(2020, 12, 1))", "isBeforeOrEqualTo(LocalDate.of(2020, 12, 1))",
						IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnLocalDateMethods")
	void visit_AssertionsWithLocalDateMethods_shouldTransform(String originalInvocation, String expectedInvocation,
			String booleanAssertion)
			throws Exception {

		fixture.addImport(java.time.LocalDate.class.getName());

		String variableDeclaration = "LocalDate locatDate = LocalDate.of(2020, 11, 1);";
		String original = String.format(
				"" +
						"		%s\n" +
						"		assertThat(locatDate.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		assertThat(locatDate).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnLocalDateMethods_notTransforming() throws Exception {
		return Stream.of(
				Arguments.of("isLeapYear()", IS_TRUE),
				Arguments.of("isSupported(ChronoField.YEAR)", IS_TRUE),
				Arguments.of("isSupported(ChronoUnit.YEARS)", IS_TRUE),
				Arguments.of("isLeapYear()", IS_FALSE),
				Arguments.of("isSupported(ChronoField.SECOND_OF_MINUTE)", IS_FALSE),
				Arguments.of("isSupported(ChronoUnit.SECONDS)", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnLocalDateMethods_notTransforming")
	void visit_AssertionsWithLocalDateMethods_shouldNotTransform(String originalInvocation, String booleanAssertion)
			throws Exception {

		fixture.addImport(java.time.LocalDate.class.getName());
		fixture.addImport(java.time.temporal.ChronoField.class.getName());
		fixture.addImport(java.time.temporal.ChronoUnit.class.getName());

		String variableDeclaration = "LocalDate locatDate = LocalDate.of(2020, 11, 1);";
		String original = String.format(
				"" +
						"		%s\n" +
						"		assertThat(locatDate.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		assertNoChange(original);
	}

	public static Stream<Arguments> assertionsOnStreamMethods() throws Exception {
		return Stream.of(
				Arguments.of("equals(stringStream)", "isEqualTo(stringStream)", IS_TRUE),
				Arguments.of("allMatch(s -> !s.isEmpty())", "allMatch(s -> !s.isEmpty())", IS_TRUE),
				Arguments.of("anyMatch(\"str-1\"::equals)", "anyMatch(\"str-1\"::equals)", IS_TRUE),
				Arguments.of("noneMatch(\"str-5\"::equals)", "noneMatch(\"str-5\"::equals)", IS_TRUE),
				Arguments.of("equals(Stream.of(\"str-1\", \"str-3\"))", "isNotEqualTo(Stream.of(\"str-1\", \"str-3\"))",
						IS_FALSE),
				Arguments.of("anyMatch(\"str-3\"::equals)", "noneMatch(\"str-3\"::equals)", IS_FALSE),
				Arguments.of("noneMatch(\"str-1\"::equals)", "anyMatch(\"str-1\"::equals)", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnStreamMethods")
	void visit_AssertionsWithStreamMethods_shouldTransform(String originalInvocation, String expectedInvocation,
			String booleanAssertion)
			throws Exception {

		fixture.addImport(java.util.stream.Stream.class.getName());

		String variableDeclaration = "Stream<String> stringStream = Stream.of(\"str-1\", \"str-2\");";
		String original = String.format(
				"" +
						"		%s\n" +
						"		assertThat(stringStream.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		assertThat(stringStream).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnDoubleStreamMethods() throws Exception {
		return Stream.of(
				Arguments.of("equals(doubleStream)", "isEqualTo(doubleStream)", IS_TRUE),
				Arguments.of("allMatch(i -> i > 0)", "allMatch(i -> i > 0)", IS_TRUE),
				Arguments.of("anyMatch(i -> i == 1)", "anyMatch(i -> i == 1)", IS_TRUE),
				Arguments.of("noneMatch(i -> i == 2)", "noneMatch(i -> i == 2)", IS_TRUE),
				Arguments.of("equals(DoubleStream.of(1.0, 3.0))", "isNotEqualTo(DoubleStream.of(1.0, 3.0))",
						IS_FALSE),
				Arguments.of("anyMatch(i -> i == 2)", "noneMatch(i -> i == 2)", IS_FALSE),
				Arguments.of("noneMatch(i -> i == 1)", "anyMatch(i -> i == 1)", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnDoubleStreamMethods")
	void visit_AssertionsWithDoubleStreamMethods_shouldTransform(String originalInvocation, String expectedInvocation,
			String booleanAssertion)
			throws Exception {

		fixture.addImport(java.util.stream.DoubleStream.class.getName());

		String variableDeclaration = "DoubleStream doubleStream = DoubleStream.of(1.0, 3.0);";
		String original = String.format(
				"" +
						"		%s\n" +
						"		assertThat(doubleStream.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		assertThat(doubleStream).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnIteratorMethods() throws Exception {
		return Stream.of(
				Arguments.of("equals(iterator)", "isEqualTo(iterator)", IS_TRUE),
				Arguments.of("hasNext()", "hasNext()", IS_TRUE),
				Arguments.of("equals(iterator)", "isNotEqualTo(iterator)", IS_FALSE),
				Arguments.of("hasNext()", "isExhausted()", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnIteratorMethods")
	void visit_AssertionsWithIteratorMethods_shouldTransform(String originalInvocation, String expectedInvocation,
			String booleanAssertion)
			throws Exception {

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.Iterator.class.getName());

		String variableDeclaration = "Iterator<String> iterator = Arrays.asList(\"str-1\", \"str-2\").listIterator();";
		String original = String.format(
				"" +
						"		%s\n" +
						"		assertThat(iterator.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		assertThat(iterator).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@MethodSource("assertionsOnIteratorMethods")
	void visit_AssertionsWithListIteratorMethods_shouldTransform(String originalInvocation, String expectedInvocation,
			String booleanAssertion)
			throws Exception {

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.ListIterator.class.getName());

		String variableDeclaration = "ListIterator<String> iterator = Arrays.asList(\"str-1\", \"str-2\").listIterator();";
		String original = String.format(
				"" +
						"		%s\n" +
						"		assertThat(iterator.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		assertThat(iterator).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnObjectEqualsMethod() throws Exception {
		return Stream.of(
				Arguments.of("equals(o)", "isEqualTo(o)", IS_TRUE),
				Arguments.of("equals(o)", "isNotEqualTo(o)", IS_FALSE));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnObjectEqualsMethod")
	void visit_AssertionsWithObjectEqualsMethod_shouldTransform(String originalInvocation, String expectedInvocation,
			String booleanAssertion)
			throws Exception {

		String variableDeclaration = "Object o = new Object();";
		String original = String.format(
				"" +
						"		%s\n" +
						"		assertThat(o.%s).%s;",
				variableDeclaration, originalInvocation, booleanAssertion);

		String expected = String.format(
				"" +
						"		%s\n" +
						"		assertThat(o).%s;",
				variableDeclaration, expectedInvocation);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnPredicateTestIsTrue() throws Exception {
		return Stream.of(
				Arguments.of("Predicate", "Predicate<String>", "s -> s.isEmpty()", "\"\""),
				Arguments.of("IntPredicate", "IntPredicate", "i -> i > 0", "1"),
				Arguments.of("LongPredicate", "LongPredicate", "i -> i > 0L", "1L"),
				Arguments.of("DoublePredicate", "DoublePredicate", "i -> i > 0.0", "1.0"));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnPredicateTestIsTrue")
	void visit_AssertionsWithPredicateTestIsTrue_shouldTransform(String className, String predicateType, String lambda,
			String valueUnderTest) throws Exception {

		fixture.addImport("java.util.function." + className);
		String original = String.format(
				"" +
						"		%s predicate = %s;" +
						"		assertThat(predicate.test(%s)).isTrue();",
				predicateType, lambda, valueUnderTest);

		String expected = String.format(
				"" +
						"		%s predicate = %s;" +
						"		assertThat(predicate).accepts(%s);",
				predicateType, lambda, valueUnderTest);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnPredicateTestIsFalse() throws Exception {
		return Stream.of(
				Arguments.of("Predicate", "Predicate<String>", "s -> s.isEmpty()", "\"not-empty\""),
				Arguments.of("IntPredicate", "IntPredicate", "i -> i > 0", "0"),
				Arguments.of("LongPredicate", "LongPredicate", "i -> i > 0L", "0L"),
				Arguments.of("DoublePredicate", "DoublePredicate", "i -> i > 0.0", "0.0"));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnPredicateTestIsFalse")
	void visit_AssertionsWithPredicateTestIsFalse_shouldTransform(String className, String predicateType, String lambda,
			String valueUnderTest) throws Exception {

		fixture.addImport("java.util.function." + className);
		String original = String.format(
				"" +
						"		%s predicate = %s;" +
						"		assertThat(predicate.test(%s)).isFalse();",
				predicateType, lambda, valueUnderTest);

		String expected = String.format(
				"" +
						"		%s predicate = %s;" +
						"		assertThat(predicate).rejects(%s);",
				predicateType, lambda, valueUnderTest);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"byte",
			"char",
			"short",
			"int",
			"long",
			"float",
			"double",
			"int",
			"Short",
			"Integer",
			"Long",
			"Byte",
			"Character",
			"Float",
			"Double",
			"Object"
	})
	void visit_OneDimensionalArrayEqualsOtherIsTrue_shouldTransform(String componentType) throws Exception {

		String original = String.format(
				"" +
						"		%s[] array = new %s[0];\n" +
						"		assertThat(array.equals(array)).isTrue();",
				componentType, componentType);

		String expected = String.format(
				"" +
						"		%s[] array = new %s[0];\n" +
						"		assertThat(array).isSameAs(array);",
				componentType, componentType);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"int",
			"Integer",
			"Object"
	})
	void visit_TwoDimensionalArrayEqualsOtherIsTrue_shouldTransform(String componentType) throws Exception {

		String original = String.format(
				"" +
						"		%s[][] array = new %s[0][0];\n" +
						"		assertThat(array.equals(array)).isTrue();",
				componentType, componentType);

		String expected = String.format(
				"" +
						"		%s[][] array = new %s[0][0];\n" +
						"		assertThat(array).isSameAs(array);",
				componentType, componentType);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"new int[0][0]",
			"new Integer[0][0]",
			"new Object[0][0]"
	})
	void visit_TwoDimensionalArrayEqualsOtherIsFalse_shouldTransform(String new2DArray) throws Exception {

		String original = String.format(
				"" +
						"		assertThat(%s.equals(%s)).isFalse();",
				new2DArray, new2DArray);

		String expected = String.format(
				"" +
						"		assertThat(%s).isNotSameAs(%s);",
				new2DArray, new2DArray);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"int",
			"Integer",
			"Object"
	})
	void visit_ThreeDimensionalArrayEqualsOtherIsTrue_shouldNotTransform(String componentType) throws Exception {

		String original = String.format(
				"" +
						"		%s[][][] array = new %s[0][0][0];\n" +
						"		assertThat(array.equals(array)).isTrue();",
				componentType, componentType);

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"boolean",
			"Boolean",
			"String"
	})
	void visit_Unsupported1DArrayEqualsOtherIsTrue_shouldNotTransform(String componentType) throws Exception {

		String original = String.format(
				"" +
						"		%s[] array = new %s[0];\n" +
						"		assertThat(array.equals(array)).isTrue();",
				componentType, componentType);

		assertNoChange(original);
	}

	@Test
	void visit_AssertionNotInExpressionStatement_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Object object = new Object();\n"
				+ "		Object isFalseReturnValue = assertThat(object == null).isFalse();";
		assertNoChange(original);
	}

	@Test
	void visit_AssertThatObjectIsNull_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Object o = null;\n"
				+ "		assertThat(o).isNull();";
		assertNoChange(original);
	}

	@Test
	void visit_AssertThatWithTwoArguments_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		String original = "" +
				"		Iterable<String> iterable = Arrays.asList(\"str-1\", \"str-2\");\n"
				+ "		assertThat(iterable, StringAssert.class).first();";
		assertNoChange(original);
	}

	@Test
	void visit_AssertThatNotFromAssertJ_shouldNotTransform() throws Exception {
		fixture.addImport("org.assertj.core.api.AbstractBooleanAssert");
		fixture.addImport("org.assertj.core.api.Assertions");
		String original = "" +
				"		class LocalClass {\n"
				+ "			static AbstractBooleanAssert<?> assertThat(boolean b) {\n"
				+ "				return org.assertj.core.api.Assertions.assertThat(b);\n"
				+ "			}\n"
				+ "\n"
				+ "		}\n"
				+ "		Object o = null;\n"
				+ "		LocalClass.assertThat(o == null).isTrue();";
		assertNoChange(original);

	}

	public static Stream<Arguments> numericWrapperequalsPrimitiveNumericIsFalse() throws Exception {
		return Stream.of(
				Arguments.of("java.math.BigInteger.valueOf(10)", "10"),
				Arguments.of("Double.valueOf(10.0)", "(byte) 10"),
				Arguments.of("Double.valueOf(10.0)", "(short) 10"),
				Arguments.of("Double.valueOf(10.0)", "10"),
				Arguments.of("Double.valueOf(10.0)", "10L"),
				Arguments.of("Double.valueOf(10.0)", "10.0F"),
				Arguments.of("Float.valueOf(10.0F)", "(byte) 10"),
				Arguments.of("Float.valueOf(10.0F)", "(short) 10"),
				Arguments.of("Float.valueOf(10.0F)", "10"),
				Arguments.of("Float.valueOf(10.0F)", "10L"),
				Arguments.of("Long.valueOf(10L)", "(byte) 10"),
				Arguments.of("Long.valueOf(10L)", "(short) 10"),
				Arguments.of("Long.valueOf(10L)", "10"),
				Arguments.of("Integer.valueOf(10)", "(byte) 10"),
				Arguments.of("Integer.valueOf(10)", "(short) 10"),
				Arguments.of("Short.valueOf((short) 10)", "(byte) 10"),
				Arguments.of("java.math.BigInteger.valueOf(10)", "10"));
	}

	@ParameterizedTest
	@MethodSource("numericWrapperequalsPrimitiveNumericIsFalse")
	void visit_NumericWrapperEqualsPrimitiveNumericIsFalse_shouldNotransform(String equalsExpression,
			String equalsArgument) throws Exception {

		String original = String.format("assertThat(%s.equals(%s)).isFalse();",
				equalsExpression, equalsArgument);
		assertNoChange(original);
	}

	@Test
	void visit_ObjectEqualsPrimitiveIntegerIsFalse_shouldTransform() throws Exception {
		String original = "" +
				"		Object object = new Object();\n"
				+ "		assertThat(object.equals(10)).isFalse();";

		String expected = "" +
				"		Object object = new Object();\n"
				+ "		assertThat(object).isNotEqualTo(10);";

		assertChange(original, expected);
	}

	@Test
	void visit_ObjectEqualsPrimitiveIntegerIsTrue_shouldTransform() throws Exception {
		String original = "" +
				"		Object object = Integer.valueOf(10);\n"
				+ "		assertThat(object.equals(10)).isTrue();";

		String expected = "" +
				"		Object object = Integer.valueOf(10);\n"
				+ "		assertThat(object).isEqualTo(10);";

		assertChange(original, expected);

	}

}