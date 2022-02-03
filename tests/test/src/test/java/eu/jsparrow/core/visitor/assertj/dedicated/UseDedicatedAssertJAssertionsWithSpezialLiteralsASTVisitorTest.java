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
public class UseDedicatedAssertJAssertionsWithSpezialLiteralsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		setVisitor(new UseDedicatedAssertJAssertionsASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(o != null).isTrue()",
			"assertThat(null != o).isTrue()",
			"assertThat(o == null).isFalse()",
			"assertThat(null == o).isFalse()",
			"assertThat(o).isNotEqualTo(null)",
			"assertThat(o).isNotSameAs(null)"
	})
	void visit_ObjectIsNotNull_shouldTransform(String originalInvocation) throws Exception {
		String original = String.format("" +
				"		Object o = new Object();\n" +
				"		%s;",
				originalInvocation);

		String expected = "" +
				"		Object o = new Object();\n"
				+ "		assertThat(o).isNotNull();";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(o == null).isTrue()",
			"assertThat(null == o).isTrue()",
			"assertThat(o != null).isFalse()",
			"assertThat(null != o).isFalse()",
			"assertThat(o).isEqualTo(null)",
			"assertThat(o).isSameAs(null)"
	})
	void visit_ObjectIsNull_shouldTransform(String originalInvocation) throws Exception {
		String original = String.format("" +
				"		Object o = null;\n" +
				"		%s;",
				originalInvocation);

		String expected = "" +
				"		Object o = null;\n"
				+ "		assertThat(o).isNull();";

		assertChange(original, expected);
	}

	@Test
	void visit_NullIsNull_shouldNotTransform() throws Exception {
		String original = "" +
				"		assertThat(null == null).isTrue();";

		assertNoChange(original);
	}

	@Test
	void visit_ObjectEqualsNullIsTrue_shouldNotTransform() throws Exception {
		String original = "" +
				"			Object o = null;\n"
				+ "			assertThat(o.equals(null)).isTrue();";

		assertNoChange(original);
	}

	@Test
	void visit_ObjectEqualsNullIsFalse_shouldTransform() throws Exception {
		String original = "" +
				"		Object o = new Object();\n"
				+ "		assertThat(o.equals(null)).isFalse();";

		String expected = "" +
				"		Object o = new Object();\n"
				+ "		assertThat(o).isNotNull();";

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsWithInfixAndZeroLiteral() throws Exception {
		return Stream.of(
				Arguments.of("==", "isZero"),
				Arguments.of("!=", "isNotZero"),
				Arguments.of(">", "isPositive"),
				Arguments.of("<", "isNegative"),
				Arguments.of(">=", "isNotNegative"),
				Arguments.of("<=", "isNotPositive"));
	}

	@ParameterizedTest
	@MethodSource("assertionsWithInfixAndZeroLiteral")
	void visit_InfixWithZeroIsTrue_shouldTransform(String infix, String newAssertion)
			throws Exception {

		String original = String.format("" +
				"		int x = 0;\n"
				+ "		assertThat(x %s 0).isTrue();", infix);

		String expected = String.format("" +
				"		int x = 0;\n"
				+ "		assertThat(x).%s();", newAssertion);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsWithZeroLiteralAsArrgument() throws Exception {
		return Stream.of(
				Arguments.of("isEqualTo", "isZero"),
				Arguments.of("isNotEqualTo", "isNotZero"),
				Arguments.of("isGreaterThan", "isPositive"),
				Arguments.of("isGreaterThanOrEqualTo", "isNotNegative"),
				Arguments.of("isLessThan", "isNegative"),
				Arguments.of("isLessThanOrEqualTo", "isNotPositive"));
	}

	@ParameterizedTest
	@MethodSource("assertionsWithZeroLiteralAsArrgument")
	void visit_AssertionsWithZeroLiteralAsArgument_shouldTransform(String oldAssertion, String newAssertion)
			throws Exception {

		String original = String.format("" +
				"		int x = 0;\n"
				+ "		assertThat(x).%s(0);", oldAssertion);

		String expected = String.format("" +
				"		int x = 0;\n"
				+ "		assertThat(x).%s();", newAssertion);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertThatValueIsEqualToZeroLiteral() throws Exception {
		return Stream.of(
				Arguments.of("0", "0"),
				Arguments.of("0L", "0"),
				Arguments.of("0.0F", "0"),
				Arguments.of("0.0", "0"),
				Arguments.of("Integer.valueOf(0)", "0"),
				Arguments.of("Long.valueOf(0L)", "0"),
				Arguments.of("Float.valueOf(0.0F)", "0"),
				Arguments.of("Double.valueOf(0.0)", "0"),
				Arguments.of("java.math.BigInteger.valueOf(0L)", "0"),
				Arguments.of("0", "0L"),
				Arguments.of("0L", "0L"),
				Arguments.of("0.0F", "0L"),
				Arguments.of("0.0", "0L"),
				Arguments.of("Integer.valueOf(0)", "0L"),
				Arguments.of("Long.valueOf(0L)", "0L"),
				Arguments.of("Float.valueOf(0.0F)", "0L"),
				Arguments.of("Double.valueOf(0.0)", "0L"),
				Arguments.of("java.math.BigInteger.valueOf(0L)", "0L"),
				Arguments.of("0.0F", "0.0F"),
				Arguments.of("0.0", "0.0F"),
				Arguments.of("Float.valueOf(0.0F)", "0.0F"),
				Arguments.of("Double.valueOf(0.0)", "0.0F"),
				Arguments.of("0.0", "0.0"),
				Arguments.of("Double.valueOf(0.0)", "0.0"));
	}

	@ParameterizedTest
	@MethodSource("assertThatValueIsEqualToZeroLiteral")
	void visit_AssertThatValueIsEqualToZeroLiteral_shouldTransform(String value, String zeroLiteral)
			throws Exception {

		String original = String.format("" +
				"assertThat(%s).isEqualTo(%s);", value, zeroLiteral);

		String expected = String.format("" +
				"assertThat(%s).isZero();", value, zeroLiteral);

		assertChange(original, expected);
	}

	@Test
	void visit_DoubleWrapperIsEqualToZero_shouldTransform() throws Exception {
		String original = "" +
				"		Double x = Double.valueOf(0.0);\n"
				+ "		assertThat(x).isEqualTo(0);";

		String expected = "" +
				"		Double x = Double.valueOf(0.0);\n"
				+ "		assertThat(x).isZero();";

		assertChange(original, expected);
	}

	@Test
	void visit_DoubleWrapperIsNotEqualToZero_shouldTransform() throws Exception {
		String original = "" +
				"		Double x = Double.valueOf(1.0);\n"
				+ "		assertThat(x).isNotEqualTo(0);";

		String expected = "" +
				"		Double x = Double.valueOf(1.0);\n"
				+ "		assertThat(x).isNotZero();";

		assertChange(original, expected);

	}

	@Test
	void visit_DoubleWrapperEqualsZero_shouldTransform() throws Exception {
		String original = "" +
				"		Double x = Double.valueOf(0.0);\n"
				+ "		assertThat(x.equals(0.0)).isTrue();";

		String expected = "" +
				"		Double x = Double.valueOf(0.0);\n"
				+ "		assertThat(x).isZero();";

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

	@Test
	void visit_ObjectIsNotEqualToZero_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Object o = new Object();\n"
				+ "		assertThat(o).isNotEqualTo(0);";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"assertThat(integer.equals(10)).isEqualTo(true)",
			"assertThat(!integer.equals(10)).isEqualTo(false)",
			"assertThat(!integer.equals(10)).isNotEqualTo(true)",
			"assertThat(integer.equals(10)).isNotEqualTo(false)"
	})
	void visit_IntegerEqualsValueIsEqualToBooleanLiteral_shouldTransform(String originalAssertion) throws Exception {

		String original = String.format(""
				+ "		Integer integer = Integer.valueOf(10);\n"
				+ "		%s;", originalAssertion);

		String expected = ""
				+ "		Integer integer = Integer.valueOf(10);\n"
				+ "		assertThat(integer).isEqualTo(10);";

		assertChange(original, expected);
	}

	@Test
	void visit_TrueIsSameAsTrue_shouldTransform() throws Exception {

		String original = ""
				+ "assertThat(true).isSameAs(true);";

		String expected = ""
				+ "assertThat(true).isTrue();";

		assertChange(original, expected);
	}
	
	@Test
	void visit_FalseIsNotSameAsTrue_shouldTransform() throws Exception {

		String original = ""
				+ "assertThat(false).isNotSameAs(true);";

		String expected = ""
				+ "assertThat(false).isFalse();";

		assertChange(original, expected);
	}

	@Test
	void visit_BooleanValueOfIsEqualToBooleanLiteral_shouldTransform() throws Exception {
		String original = "assertThat(Boolean.valueOf(true)).isEqualTo(true);";
		String expected = "assertThat(Boolean.valueOf(true)).isTrue();";

		assertChange(original, expected);
	}

	@Test
	void visit_BooleanVariableIsEqualToSame_shouldNotTransform() throws Exception {
		String original = ""
				+ "		Boolean booleanTrue = Boolean.valueOf(true);\n"
				+ "		assertThat(booleanTrue).isEqualTo(booleanTrue);";

		assertNoChange(original);
	}

	@Test
	void visit_BooleanValueOfIsInFalse_shouldNotTransform() throws Exception {
		String original = "assertThat(Boolean.valueOf(false)).isIn(false);";
		assertNoChange(original);

	}
}
