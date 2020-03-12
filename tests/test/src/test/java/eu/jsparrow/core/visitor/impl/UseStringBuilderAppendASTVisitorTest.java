package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

/**
 * Tests for {@link UseStringBuilderAppendASTVisitor}.
 * 
 * @since 2.7.0
 *
 */
@SuppressWarnings("nls")
public class UseStringBuilderAppendASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		visitor = new UseStringBuilderAppendASTVisitor();
	}

	@Test
	public void visit_simpleConcatenation_shouldReplace() throws Exception {
		String original = "String value = \"first\" + \"second\" + \"third\";";
		String expected = "String value = new StringBuilder().append(\"first\").append(\"second\").append(\"third\").toString();";

		assertChange(original, expected);
	}

	@Test
	public void visit_veryFewOperands_shouldNotReplace() throws Exception {
		assertNoChange("String value = \"first\" + \"second\";");
	}

	@Test
	public void visit_extendedOperands_shouldReplace() throws Exception {
		String original = "" +
				"String value = \"left\" " +
				"+ \"right\" " +
				"+ \"extendedOperand1\" " +
				"+ \"extendedOperand2\";";
		String expected = "" +
				"String value = new StringBuilder()" +
				".append(\"left\")" +
				".append(\"right\")" +
				".append(\"extendedOperand1\")" +
				".append(\"extendedOperand2\")" +
				".toString();";

		assertChange(original, expected);
	}

	@Test
	public void visit_nestedInfixExpression_shouldReplace() throws Exception {
		String original = "" +
				"String value = \"left\" " +
				"+ (\"middle\" + \"infix\" + \"expression\") " +
				"+ \"extendedOperand\";";
		String expected = "" +
				"String value = new StringBuilder()" +
				".append(\"left\")" +
				".append(\"middle\")" +
				".append(\"infix\")" +
				".append(\"expression\")" +
				".append(\"extendedOperand\")" +
				".toString();";

		assertChange(original, expected);
	}

	@Test
	public void visit_mixedExpressionTypes_shouldTransform() throws Exception {
		String original = "String value = \"left\" +  2 * 3 + \"right\";";
		String expected = "" +
				"String value = new StringBuilder()" +
				".append(\"left\")" +
				".append( 2 * 3 )" +
				".append(\"right\")" +
				".toString();";

		assertChange(original, expected);
	}

	@Test
	public void visit_nestedIntegerExpressions_shouldReplace() throws Exception {
		String original = "" +
				"String value = " +
				"\"left\" " +
				"+ (3 + 5) " +
				"+ \"extendedOperand\";";
		String expected = "" +
				"String value = new StringBuilder()" +
				".append(\"left\")" +
				".append( 3 + 5 )" +
				".append(\"extendedOperand\")" +
				".toString();";

		assertChange(original, expected);
	}

	@Test
	public void visit_nestedIntegerSubtractionExpressions_shouldReplace() throws Exception {
		String original = "" +
				"String value = " +
				"\"left\" " +
				"+ (3 - 5) " +
				"+ \"extendedOperand\";";
		String expected = "" +
				"String value = new StringBuilder()" +
				".append(\"left\")" +
				".append( 3 - 5 )" +
				".append(\"extendedOperand\")" +
				".toString();";

		assertChange(original, expected);
	}

	@Test
	public void visit_mixedExtendedOperand_shouldReplace() throws Exception {
		String original = "" +
				"String value = " +
				"\"left\" + " +
				"\"right\" + \n" +
				"	(5 + \"a\") +\n" +
				"	(2 + 4 + \"b\" + \"c\" ) + \n" +
				"	(6 + 7);";
		String expected = "" +
				"String value = new StringBuilder()" +
				".append(\"left\")" +
				".append(\"right\")" +
				".append(5)" +
				".append(\"a\")" +
				".append(2 + 4 + \"b\" + \"c\" )" +
				".append(6 + 7)" +
				".toString();";

		assertChange(original, expected);
	}

	@Test
	public void visit_numericExpression_shouldNotTransform() throws Exception {
		assertNoChange("int value = 3 + 5;");
	}

	@Test
	public void visit_numericSubtractExpression_shouldNotTransform() throws Exception {
		assertNoChange("int value = 3 - 5;");
	}

	@Test
	public void visit_stringExpressionPrefixedByInt_shouldTransform() throws Exception {
		// String value2 = 2 + 4 + "b" + "c";
		String original = "String value = 2 + \"b\" + \"c\";";

		String expected = "" +
				"String value = new StringBuilder()" +
				".append(2)" +
				".append(\"b\")" +
				".append(\"c\")"
				+ ".toString();";

		assertChange(original, expected);
	}

	@Test
	public void visit_numericExpressionFollowedByString_shouldNotTransform() throws Exception {
		assertNoChange("String value = 2 + 4 + \"b\" + \"c\" + \"d\";");
	}

	@Test
	public void visit_numericExpressionSurroundedByString_shouldNotTransform() throws Exception {
		String original = "String value = 1 + \"a\" + 2 * 3 + \"value\";";
		String expected = "String value = new StringBuilder()" +
				".append(1)" +
				".append(\"a\")" +
				".append(2 * 3)" +
				".append(\"value\")" +
				".toString();";

		assertChange(original, expected);
	}

	@Test
	public void visit_expressionWithNullLiteral_shouldNotTransform() throws Exception {
		String block = "String value = \"\" + null;";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}
}
