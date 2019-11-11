package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class RemoveNullCheckBeforeInstanceofASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private RemoveNullCheckBeforeInstanceofASTVisitor visitor;

	@BeforeEach
	public void setUp() {
		visitor = new RemoveNullCheckBeforeInstanceofASTVisitor();
	}

	@Test
	public void visit_infixExpressionAndNullCheck_shouldReplace() throws Exception {
		String block = "String value = \"\"; if (value != null && value instanceof String) { }";
		String expectedContent = "String value = \"\"; if (value instanceof String) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_nullLiteralInLHS_shouldReplace() throws Exception {
		String block = "String value = \"\"; if (null != value && value instanceof String) { }";
		String expectedContent = "String value = \"\"; if (value instanceof String) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_initializationExpression_shouldReplace() throws Exception {
		String block = "String value = \"\"; boolean b = value != null && value instanceof String;";
		String expectedContent = "String value = \"\"; boolean b = value instanceof String;";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_multipleOperands_shouldReplace() throws Exception {
		String block = "String value = \"\"; if (value != null && value instanceof String && value.isEmpty()) { }";
		String expectedContent = "String value = \"\"; if (value instanceof String && value.isEmpty()) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_infixExpressionOrNullCheck_shouldReplace() throws Exception {
		String block = "String value = \"\"; if (value == null || !(value instanceof String)) { }";
		String expectedContent = "String value = \"\"; if (!(value instanceof String)) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_multipleOperandsOr_shouldReplace() throws Exception {
		String block = "String value = \"\"; if (value == null || !(value instanceof String) || value.isEmpty()) { }";
		String expectedContent = "String value = \"\"; if (!(value instanceof String) || value.isEmpty()) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_nestedParenthesizedExpression_shouldReplace() throws Exception {
		String block = "String value = \"\"; if (value == null || !(((value instanceof String)))) { }";
		String expectedContent = "String value = \"\"; if (!(((value instanceof String)))) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(expectedContent);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_missingNullCheck_shouldNotReplace() throws Exception {
		String block = "String value = \"\"; if (value instanceof String) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_missingNullCheckNegatedExpression_shouldNotReplace() throws Exception {
		String block = "String value = \"\"; if (!(value instanceof String)) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_mismatchNotEqualsNullCheckOperator_shouldNotReplace() throws Exception {
		String block = "String value = \"\"; if (value == null && value instanceof String) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_mismatchEqualsNullCheckOperator_shouldNotReplace() throws Exception {
		String block = "String value = \"\"; if (value != null || !(value instanceof String)) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_missingPrefixOperator_shouldNotReplace() throws Exception {
		String block = "String value = \"\"; if (value == null || (value instanceof String)) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_missingPrefixOperatorNestedParenthesizedExpression_shouldNotReplace() throws Exception {
		String block = "String value = \"\"; if (value == null || (((((value instanceof String)))))) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_missingSimpleNameInInstanceof_shouldNotReplace() throws Exception {
		String block = "String block = \" \"; if(block != null && block.substring(0) instanceof String) {}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_mismatchingInfixCondition_shouldNotReplace() throws Exception {
		String block = "String value = \"\"; if (value != null || value instanceof String) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_mismatchingNullCheckAndInstanceofLHSs_shouldNotReplace() throws Exception {
		String block = "" + "BufferedReader r;\n" + "String line;\n"
				+ "while ((line = r.readLine()) != null && line instanceof String) {}";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_missingNullLiteral_shouldNotReplace() throws Exception {
		String block = "String value, value2 = \"\"; if (value != value2 && value instanceof String) { }";
		fixture.addMethodBlock(block);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock(block);
		assertMatch(expected, fixture.getMethodBlock());
	}

}
