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
public class UseDedicatedAssertJAssertionsWithLiteralsASTVisitorTest extends UsesSimpleJDTUnitFixture {

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
}
