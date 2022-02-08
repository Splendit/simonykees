package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

@SuppressWarnings("nls")
public class UseDedicatedAssertJAssertionsWithSizeOrLengthASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		setVisitor(new UseDedicatedAssertJAssertionsASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"ArrayList",
			"TreeMap",
			"HashMap",
			"HashSet"
	})
	void visit_AssertionsWithSizeOfDifferentTypes_shouldTransform(String typeName) throws Exception {
		fixture.addImport("java.util." + typeName);

		String original = String.format("" +
				"assertThat(new %s<>().size()).isZero();", typeName);

		String expected = String.format("" +
				"assertThat(new %s<>()).isEmpty();", typeName);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(list.size() == list.size()).isTrue()",
			"assertThat(list.size()).isEqualTo(list.size())",
			"assertThat(list).hasSize(list.size())"
	})
	void visit_AssertionWithListsOfSameSize_shouldTransform(String originalAssertion) throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = String.format("" +
				"		List<Object> list = Arrays.asList(new Object());\n" +
				"		%s;", originalAssertion);
		String expected = "" +
				"		List<Object> list = Arrays.asList(new Object());\n" +
				"		assertThat(list).hasSameSizeAs(list);";
		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(string.length() == string.length()).isTrue()",
			"assertThat(string.length()).isEqualTo(string.length())",
			"assertThat(string).hasSize(string.length())"
	})
	void visit_AssertionWithStringsOfSameSize_shouldTransform(String originalAssertion) throws Exception {
		String original = String.format("" +
				"		String string = \"ABC\";\n"
				+ "		%s;", originalAssertion);
		String expected = "" +
				"		String string = \"ABC\";\n"
				+ "		assertThat(string).hasSameSizeAs(string);";
		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(intArray.length == intArray.length).isTrue()",
			"assertThat(intArray.length).isEqualTo(intArray.length)",
			"assertThat(intArray).hasSize(intArray.length)"
	})
	void visit_AssertionWithArraysOfSameSize_shouldTransform(String originalAssertion) throws Exception {
		String original = String.format("" +
				"		int[] intArray = new int[] { 0, 0 };\n"
				+ "		%s;", originalAssertion);
		String expected = "" +
				"		int[] intArray = new int[] { 0, 0 };\n"
				+ "		assertThat(intArray).hasSameSizeAs(intArray);";
		assertChange(original, expected);
	}

	public static Stream<Arguments> relationalAssertionsOnSizeOrLength() throws Exception {
		return Stream.of(
				Arguments.of("isEqualTo", "hasSize"),
				Arguments.of("isGreaterThan", "hasSizeGreaterThan"),
				Arguments.of("isGreaterThanOrEqualTo", "hasSizeGreaterThanOrEqualTo"),
				Arguments.of("isLessThan", "hasSizeLessThan"),
				Arguments.of("isLessThanOrEqualTo", "hasSizeLessThanOrEqualTo"));
	}

	@ParameterizedTest
	@MethodSource("relationalAssertionsOnSizeOrLength")
	void visit_AssertionWithListSize_shouldTransform(String oldAssertion, String newAssertion) throws Exception {

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = String.format("" +
				"		List<Object> list = Arrays.asList();\n"
				+ "		int size = 0;\n"
				+ "		assertThat(list.size()).%s(size);", oldAssertion);

		String expected = String.format("" +
				"		List<Object> list = Arrays.asList();\n"
				+ "		int size = 0;\n"
				+ "		assertThat(list).%s(size);", newAssertion);

		assertChange(original, expected);

	}

	@ParameterizedTest
	@MethodSource("relationalAssertionsOnSizeOrLength")
	void visit_AssertionWithStringLength_shouldTransform(String oldAssertion, String newAssertion) throws Exception {

		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = String.format("" +
				"		String string = \"\";\n"
				+ "		int length = 0;\n"
				+ "		assertThat(string.length()).%s(length);", oldAssertion);

		String expected = String.format("" +
				"		String string = \"\";\n"
				+ "		int length = 0;\n"
				+ "		assertThat(string).%s(length);", newAssertion);

		assertChange(original, expected);

	}

	@ParameterizedTest
	@MethodSource("relationalAssertionsOnSizeOrLength")
	void visit_AssertionWithArrayLengthAsFieldAccess_shouldTransform(String oldAssertion, String newAssertion)
			throws Exception {

		String original = String.format(""
				+ "		int length = 0;\n"
				+ "		assertThat(new int[] {}.length).%s(length);", oldAssertion);

		String expected = String.format(""
				+ "		int length = 0;\n"
				+ "		assertThat(new int[] {}).%s(length);", newAssertion);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@MethodSource("relationalAssertionsOnSizeOrLength")
	void visit_AssertionWithArrayLengthAsQualifiedName_shouldTransform(String oldAssertion, String newAssertion)
			throws Exception {

		String original = String.format(""
				+ "		int length = 0;\n"
				+ "		int[] intArray = new int[] {};\n"
				+ "		assertThat(intArray.length).%s(length);", oldAssertion);

		String expected = String.format(""
				+ "		int length = 0;\n"
				+ "		int[] intArray = new int[] {};\n"
				+ "		assertThat(intArray).%s(length);", newAssertion);

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(emptyList.size() == 0).isTrue()",
			"assertThat(emptyList.size() <= 0).isTrue()",
			"assertThat(emptyList.size()).isEqualTo(0)",
			"assertThat(emptyList.size()).isLessThanOrEqualTo(0)",
			"assertThat(emptyList.size()).isZero()",
			"assertThat(emptyList.size()).isNotPositive()",
			"assertThat(emptyList).hasSize(0)",
			"assertThat(emptyList).hasSizeLessThanOrEqualTo(0)"
	})
	void visit_EmptyListSizeAndZeroLiteral_shouldTransform(String originalAssertion) throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = String.format("" +
				"		List<Object> emptyList = Arrays.asList();\n"
				+ "		%s;", originalAssertion);

		String expected = "" +
				"		List<Object> emptyList = Arrays.asList();\n"
				+ "		assertThat(emptyList).isEmpty();";

		assertChange(original, expected);

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(list.size() != 0).isTrue()",
			"assertThat(list.size() > 0).isTrue()",
			"assertThat(list.size()).isNotEqualTo(0)",
			"assertThat(list.size()).isGreaterThan(0)",
			"assertThat(list.size()).isNotZero()",
			"assertThat(list.size()).isPositive()",
			"assertThat(list).hasSizeGreaterThan(0)"
	})
	void visit_NotEmptyListSizeAndZeroLiteral_shouldTransform(String originalAssertion) throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = String.format("" +
				"		List<Object> list = Arrays.asList(new Object());\n"
				+ "		%s;", originalAssertion);

		String expected = "" +
				"		List<Object> list = Arrays.asList(new Object());\n"
				+ "		assertThat(list).isNotEmpty();";

		assertChange(original, expected);

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"isNotIn(0)",
			"isEven()",
			"isIn(Arrays.asList(1, 2))"
	})
	void visit_NotSupportedAssertionWithListSize_shouldNotTransform(String notSupportedAssertion) throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());

		String original = String.format("" +
				"		List<Integer> list = Arrays.asList(1, 2);\n" +
				"		assertThat(list.size()).%s;", notSupportedAssertion);
		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"hasSizeLessThanOrEqualTo(list.size())",
			"hasSize(Arrays.asList(1).size())",
			"hasSize(1)",
			"hasSize(\"x\".length())",
			"hasSize(new HashMap<Object, Object>().size())"
	})
	void visit_NotSupportedHasSizeAssertions_shouldNotTransform(String originalAssertion) throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		fixture.addImport(java.util.List.class.getName());
		fixture.addImport(java.util.HashMap.class.getName());

		String original = String.format("" +
				"		List<Object> list = Arrays.asList(new Object());\n" +
				"		assertThat(list).%s;", originalAssertion);
		assertNoChange(original);
	}

	public static Stream<Arguments> notSupportedSizeAndLength() throws Exception {
		return Stream.of(
				Arguments.of(""
						+ "			int size() {\n"
						+ "				return 10;\n"
						+ "			}",
						"size()"),
				Arguments.of(""
						+ "			int getSize() {\n"
						+ "				return 10;\n"
						+ "			}",
						"getSize()"),
				Arguments.of(""
						+ "			int length() {\n"
						+ "				return 10;\n"
						+ "			}",
						"length()"),
				Arguments.of(""
						+ "			int size(String s) {\n"
						+ "				return s.length();\n"
						+ "			}",
						"size(\"0123456789\")"));
	}

	@ParameterizedTest
	@MethodSource("notSupportedSizeAndLength")
	void visit_SizeMethodDeclaredByLocalClass_shouldNotTransform(String methodDeclaration, String invocation)
			throws Exception {
		String original = String.format(""
				+ "		class LocalClass {\n"
				+ "%s\n"
				+ "		}\n"
				+ "		assertThat(new LocalClass().%s).isEqualTo(10);",
				methodDeclaration, invocation);
		assertNoChange(original);
	}
}
