package eu.jsparrow.core.visitor.impl.ternary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.Expression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

@SuppressWarnings({ "nls" })
public class SupportedExpressionComplexityVisitorTest {

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
	void testIsSupportedExpression_shouldReturnTrue(String code) throws Exception {
		Expression expressionToTest = ASTNodeBuilder.createExpressionFromString(code);
		SupportedExpressionComplexityVisitor visitor = new SupportedExpressionComplexityVisitor();
		expressionToTest.accept(visitor);
		assertTrue(visitor.isSupportedExpression());
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
	void testIsSupportedExpression_shouldReturnFalse(String code) throws Exception {
		Expression expressionToTest = ASTNodeBuilder.createExpressionFromString(code);
		SupportedExpressionComplexityVisitor visitor = new SupportedExpressionComplexityVisitor();
		expressionToTest.accept(visitor);
		assertFalse(visitor.isSupportedExpression());
	}
	
	
	@Test
	void testBooleanFalseLiteralComplexity() throws Exception {
		Expression expressionToTest = ASTNodeBuilder.createExpressionFromString("false");
		SupportedExpressionComplexityVisitor visitor = new SupportedExpressionComplexityVisitor();
		expressionToTest.accept(visitor);
		assertEquals(4, visitor.getTotalComplexity());
	}
	
	@Test
	void testBooleanTrueLiteralComplexity() throws Exception {
		Expression expressionToTest = ASTNodeBuilder.createExpressionFromString("true");
		SupportedExpressionComplexityVisitor visitor = new SupportedExpressionComplexityVisitor();
		expressionToTest.accept(visitor);
		assertEquals(4, visitor.getTotalComplexity());
	}
	
	@Test
	void testNullLiteralComplexity() throws Exception {
		Expression expressionToTest = ASTNodeBuilder.createExpressionFromString("null");
		SupportedExpressionComplexityVisitor visitor = new SupportedExpressionComplexityVisitor();
		expressionToTest.accept(visitor);
		assertEquals(4, visitor.getTotalComplexity());
	}
	
	@Test
	void testClassLiteralComplexity() throws Exception {
		Expression expressionToTest = ASTNodeBuilder.createExpressionFromString("X.class");
		SupportedExpressionComplexityVisitor visitor = new SupportedExpressionComplexityVisitor();
		expressionToTest.accept(visitor);
		assertEquals(6, visitor.getTotalComplexity());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"1234567890 + 1234567890",
			"(1234567890 + 1234567890)",
			"12345 + 67890 + 12345 + 67890",
			"x123456789y123456789"
	})
	void testGetTotalComplexity_alwaysEquals20(String code) throws Exception {
		Expression expressionToTest = ASTNodeBuilder
			.createExpressionFromString(code);
		SupportedExpressionComplexityVisitor visitor = new SupportedExpressionComplexityVisitor();
		expressionToTest.accept(visitor);
		assertEquals(20, visitor.getTotalComplexity());
	}
}
