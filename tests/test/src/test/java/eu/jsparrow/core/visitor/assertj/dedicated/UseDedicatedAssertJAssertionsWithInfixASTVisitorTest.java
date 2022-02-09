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
public class UseDedicatedAssertJAssertionsWithInfixASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private static final String IS_TRUE = "isTrue()";
	private static final String IS_FALSE = "isFalse()";

	@BeforeEach
	void setUp() throws Exception {
		setVisitor(new UseDedicatedAssertJAssertionsASTVisitor());
		addDependency("org.assertj", "assertj-core", "3.21.0");
		fixture.addImport("org.assertj.core.api.Assertions.assertThat", true, false);
	}

	public static Stream<Arguments> assertionsOnInfixOperationsWithObject() throws Exception {
		return Stream.of(
				Arguments.of("o == o", IS_TRUE, "isSameAs(o)"),
				Arguments.of("o != o", IS_TRUE, "isNotSameAs(o)"),
				Arguments.of("o == o", IS_FALSE, "isNotSameAs(o)"),
				Arguments.of("o != o", IS_FALSE, "isSameAs(o)"));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnInfixOperationsWithObject")
	void visit_InfixOperationWithObject_shouldTransform(String infixOperation, String booleanAssertion,
			String newAssertion) throws Exception {

		String original = String.format("" +
				"		Object o = new Object();\n"
				+ "		assertThat(%s).%s;",
				infixOperation, booleanAssertion);

		String expected = String.format("" +
				"		Object o = new Object();\n"
				+ "		assertThat(o).%s;",
				newAssertion);
		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnInfixOperationsWith1DIntArray() throws Exception {
		return Stream.of(
				Arguments.of("intArray == intArray", IS_TRUE, "isSameAs(intArray)"),
				Arguments.of("intArray != new int[0]", IS_TRUE, "isNotSameAs(new int[0])"),
				Arguments.of("intArray != intArray", IS_FALSE, "isSameAs(intArray)"),
				Arguments.of("intArray == new int[0]", IS_FALSE, "isNotSameAs(new int[0])"));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnInfixOperationsWith1DIntArray")
	void visit_InfixOperationWith1DIntArray_shouldTransform(String infixOperation, String booleanAssertion,
			String newAssertion) throws Exception {

		String original = String.format("" +
				"		int[] intArray = new int[0];\n"
				+ "		assertThat(%s).%s;",
				infixOperation, booleanAssertion);

		String expected = String.format("" +
				"		int[] intArray = new int[0];\n"
				+ "		assertThat(intArray).%s;",
				newAssertion);

		assertChange(original, expected);
	}

	public static Stream<Arguments> assertionsOnInfixOperationsWithInt() throws Exception {
		return Stream.of(
				Arguments.of("x == 10", IS_TRUE, "isEqualTo(10)"),
				Arguments.of("x != 11", IS_TRUE, "isNotEqualTo(11)"),
				Arguments.of("x < 11", IS_TRUE, "isLessThan(11)"),
				Arguments.of("x <= 11", IS_TRUE, "isLessThanOrEqualTo(11)"),
				Arguments.of("x > 9", IS_TRUE, "isGreaterThan(9)"),
				Arguments.of("x >= 9", IS_TRUE, "isGreaterThanOrEqualTo(9)"),
				Arguments.of("x != 10", IS_FALSE, "isEqualTo(10)"),
				Arguments.of("x == 11", IS_FALSE, "isNotEqualTo(11)"),
				Arguments.of("x < 9", IS_FALSE, "isGreaterThanOrEqualTo(9)"),
				Arguments.of("x <= 9", IS_FALSE, "isGreaterThan(9)"),
				Arguments.of("x > 11", IS_FALSE, "isLessThanOrEqualTo(11)"),
				Arguments.of("x >= 11", IS_FALSE, "isLessThan(11)"),
				Arguments.of("!(x == 10)", IS_FALSE, "isEqualTo(10)"),
				Arguments.of("!(x != 11)", IS_FALSE, "isNotEqualTo(11)"),
				Arguments.of("!(x < 11)", IS_FALSE, "isLessThan(11)"),
				Arguments.of("!(x <= 11)", IS_FALSE, "isLessThanOrEqualTo(11)"),
				Arguments.of("!(x > 9)", IS_FALSE, "isGreaterThan(9)"),
				Arguments.of("!(x >= 9)", IS_FALSE, "isGreaterThanOrEqualTo(9)"),
				Arguments.of("!(x != 10)", IS_TRUE, "isEqualTo(10)"),
				Arguments.of("!(x == 11)", IS_TRUE, "isNotEqualTo(11)"),
				Arguments.of("!(x < 9)", IS_TRUE, "isGreaterThanOrEqualTo(9)"),
				Arguments.of("!(x <= 9)", IS_TRUE, "isGreaterThan(9)"),
				Arguments.of("!(x > 11)", IS_TRUE, "isLessThanOrEqualTo(11)"),
				Arguments.of("!(x >= 11)", IS_TRUE, "isLessThan(11)"));
	}

	@ParameterizedTest
	@MethodSource("assertionsOnInfixOperationsWithInt")
	void visit_InfixOperationWithInt_shouldTransform(String infixOperation, String booleanAssertion,
			String newAssertion) throws Exception {

		String original = String.format("" +
				"		int x = 10;\n"
				+ "		assertThat(%s).%s;",
				infixOperation, booleanAssertion);

		String expected = String.format("" +
				"		int x = 10;\n"
				+ "		assertThat(x).%s;",
				newAssertion);

		assertChange(original, expected);
	}

	public static Stream<Arguments> infixEqualsWithPrimitiveTypes() throws Exception {
		return Stream.of(
				Arguments.of("boolean", "true"),
				Arguments.of("char", "'A'"),
				Arguments.of("byte", "10"),
				Arguments.of("short", "10"),
				Arguments.of("int", "10"),
				Arguments.of("long", "10L"),
				Arguments.of("float", "10.0F"),
				Arguments.of("double", "10.0"));
	}

	@ParameterizedTest
	@MethodSource("infixEqualsWithPrimitiveTypes")
	void visit_InfixEqualsWithPrimitiveTypes_shouldTransform(String numericType, String literal)
			throws Exception {

		String original = String.format("" +
				"		%s x1 = %s;\n"
				+ "		%s x2 = %s;\n"
				+ "		assertThat(x1 == x2).isTrue();\n",
				numericType, literal, numericType, literal);

		String expected = String.format("" +
				"		%s x1 = %s;\n"
				+ "		%s x2 = %s;\n"
				+ "		assertThat(x1).isEqualTo(x2);\n",
				numericType, literal, numericType, literal);

		assertChange(original, expected);
	}

	@Test
	void visit_ExtendedInfixOperands_shouldNotTransform() throws Exception {
		String original = ""
				+ "		boolean a = false;\n"
				+ "		assertThat(a || false || true).isTrue();";

		assertNoChange(original);

	}

	@ParameterizedTest
	@ValueSource(strings = {
			"||",
			"&&",
			"|",
			"&",
			"^",
	})
	void visit_NotSupportedInfixOperator_shouldNotTransform(String notSupportedInfix) throws Exception {
		String original = String.format(""
				+ "		boolean a = true;\n"
				+ "		assertThat(a %s true).isTrue();",
				notSupportedInfix);

		assertNoChange(original);
	}

	/**
	 * This test may fail in the future as soon as some infix operations with
	 * different types are be supported.
	 * 
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"new Object() != Long.valueOf(0L)",
			"Integer.valueOf(0) == 0",
			"0L == 0",
			"0.0 == 0",
	})
	void visit_InfixOperandsOfDifferenttype_shouldNotTransform(String unsupportedInfixExpression) throws Exception {
		String original = String.format(
				"assertThat(%s).isTrue();",
				unsupportedInfixExpression);

		assertNoChange(original);
	}
	
	
	@Test
	void visit_InfixOperandsOfNotSupportedType_shouldNotTransform()  throws Exception {
		String original = ""
				+ "		int[][][] array3D = new int[0][0][0];\n"
				+ "		assertThat(array3D == array3D).isTrue();";

		assertNoChange(original);
	}
}
