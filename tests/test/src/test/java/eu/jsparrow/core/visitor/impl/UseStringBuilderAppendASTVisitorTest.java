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

	private UseStringBuilderAppendASTVisitor visitor;

	@BeforeEach
	public void setUp() {
		visitor = new UseStringBuilderAppendASTVisitor();
	}

	@Test
	public void visit_simpleConcatenation_shouldReplace() throws Exception {
		String block = "String value = \"first\" + \"second\" + \"third\";";
		String expected = "String value = new StringBuilder().append(\"first\").append(\"second\").append(\"third\").toString();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_veryFewOperands_shouldNotReplace() throws Exception {
		String block = "String value = \"first\" + \"second\";";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_extendedOperands_shouldReplace() throws Exception {
		String block = "String value = \"left\" " + "+ \"right\" " + "+ \"extendedOperand1\" "
				+ "+ \"extendedOperand2\";";
		String expected = "String value = new StringBuilder()" + ".append(\"left\")" + ".append(\"right\")"
				+ ".append(\"extendedOperand1\")" + ".append(\"extendedOperand2\")" + ".toString();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_nestedInfixExpression_shouldReplace() throws Exception {
		String block = "String value = \"left\" " + "+ (\"middle\" + \"infix\" + \"expression\") "
				+ "+ \"extendedOperand\";";
		String expected = "String value = new StringBuilder()" + ".append(\"left\")" + ".append(\"middle\")"
				+ ".append(\"infix\")" + ".append(\"expression\")" + ".append(\"extendedOperand\")" + ".toString();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_mixedExpressionTypes_shouldTransform() throws Exception {
		String block = "String value = \"left\" +  2 * 3 + \"right\";";
		String expected = "String value = new StringBuilder()" + ".append(\"left\")"
				+ ".append( 2 * 3 ).append(\"right\").toString();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_nestedIntegerExpressions_shouldReplace() throws Exception {
		String block = "String value = \"left\" " + "+ (3 + 5) " + "+ \"extendedOperand\";";
		String expected = "String value = new StringBuilder()" + ".append(\"left\")" + ".append( 3 + 5 )"
				+ ".append(\"extendedOperand\")" + ".toString();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_nestedIntegerSubtractionExpressions_shouldReplace() throws Exception {
		String block = "String value = \"left\" " + "+ (3 - 5) " + "+ \"extendedOperand\";";
		String expected = "String value = new StringBuilder()" + ".append(\"left\")" + ".append( 3 - 5 )"
				+ ".append(\"extendedOperand\")" + ".toString();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_mixedExtendedOperand_shouldReplace() throws Exception {
		String block = "String value = \"left\" + \"right\" + \n" + "	(5 + \"a\") +\n"
				+ "	(2 + 4 + \"b\" + \"c\" ) + \n" + "	(6 + 7);";
		String expected = "String value = new StringBuilder()" + ".append(\"left\")" + ".append(\"right\")"
				+ ".append(5)" + ".append(\"a\")" + ".append(2 + 4 + \"b\" + \"c\" )" + ".append(6 + 7)"
				+ ".toString();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_numericExpression_shouldNotTransform() throws Exception {
		String block = "int value = 3 + 5;"; //$NON-NLS-1$

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_numericSubtractExpression_shouldNotTransform() throws Exception {
		String block = "int value = 3 - 5;";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_stringExpressionPrefixedByInt_shouldTransform() throws Exception {
		// String value2 = 2 + 4 + "b" + "c";
		String block = "String value = 2 + \"b\" + \"c\";";

		String expected = "String value = new StringBuilder()" + ".append(2)" + ".append(\"b\")" + ".append(\"c\")"
				+ ".toString();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_numericExpressionFollowedByString_shouldNotTransform() throws Exception {
		String block = "String value = 2 + 4 + \"b\" + \"c\" + \"d\";";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(block);
		assertMatch(expectedBlock, fixture.getMethodBlock());
	}

	@Test
	public void visit_numericExpressionSurroundedByString_shouldNotTransform() throws Exception {
		String block = "String value = 1 + \"a\" + 2 * 3 + \"value\";";
		String expected = "String value = new StringBuilder()" + ".append(1)" + ".append(\"a\")" + ".append(2 * 3)"
				+ ".append(\"value\")" + ".toString();";

		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);

		Block expectedBlock = ASTNodeBuilder.createBlockFromString(expected);
		assertMatch(expectedBlock, fixture.getMethodBlock());
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
