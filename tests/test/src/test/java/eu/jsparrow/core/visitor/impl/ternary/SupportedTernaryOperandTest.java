package eu.jsparrow.core.visitor.impl.ternary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.Expression;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings({ "nls" })
public class SupportedTernaryOperandTest {

	@ParameterizedTest
	@ValueSource(strings = {
			"'a'",
			"0",
			"\"a\"",
			"\"\\\"\"",
			"true",
			"false",
			"null",
			"Integer.class",
			"Integer.MIN_VALUE",
			"x",
			"f()",
			"this",
			"this.x",
			"this.f()",
			"super.x",
			"super.f()",
			"array[1]",
			"new int[]{}",
			"new int[2]",
			"(int)x",
			"(List<Integer>)o",
			"new Object()",
			"new List<Integer>()",
			"x == y",
			"x instanceof int",
			"x++",
			"++x",
			"(x+y)"

	})
	void testIsSupportedTernaryOperand_shouldReturnTrue(String code) throws Exception {
		Expression expressionToTest = ASTNodeBuilder.createExpressionFromString(code);
		assertTrue(SupportedTernaryOperand.isSupportedTernaryOperand(expressionToTest));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "		\"\"\"\n"
					+ "			ABC\n"
					+ "		\"\"\"",
			""
					+ "		\"A\" + \"\"\"\n"
					+ "				BCD\n"
					+ "		\"\"\"",
			""
					+ "		\"\"\"\n"
					+ "			BCD\n"
					+ "		\"\"\" + \"A\"",
			"x = 1",
			"(x = 1)",
			"condition ? 1 : 0",
			"(condition ? 1 : 0)",
			"() -> {}",
			"(() -> {})",
			"new Object() {}",
			"(new Object() {})",
			"Object::new",
			"(Object::new)"
	})
	void testIsSupportedTernaryOperand_shouldReturnFalse(String code) throws Exception {
		Expression expressionToTest = ASTNodeBuilder.createExpressionFromString(code);
		assertFalse(SupportedTernaryOperand.isSupportedTernaryOperand(expressionToTest));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"12345678 + 12345678 + anotherBigNumber",
			"(12345678 + 12345678 + anotherBigNumber) + 1",
			"x123456789y123456789z123456789"
	})
	void testWithBigOperand_shouldReturnFalse(String code) throws Exception {
		Expression expressionToTest = ASTNodeBuilder
			.createExpressionFromString(code);
		assertFalse(SupportedTernaryOperand.isSupportedTernaryOperand(expressionToTest));
	}
	
	
	@ParameterizedTest
	@ValueSource(strings = {
			"1234567890 + 1234567890",
			"(1234567890 + 1234567890)",
			"12345 + 67890 + 12345 + 67890",
			"x123456789y123456789"
	})
	void testWithBiggestPossibleOperand_shouldReturnTrue(String code) throws Exception {
		Expression expressionToTest = ASTNodeBuilder
			.createExpressionFromString(code);
		assertTrue(SupportedTernaryOperand.isSupportedTernaryOperand(expressionToTest));
	}
}
