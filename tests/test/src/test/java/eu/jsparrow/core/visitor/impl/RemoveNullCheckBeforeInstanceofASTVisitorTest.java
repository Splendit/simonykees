package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class RemoveNullCheckBeforeInstanceofASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setVisitor(new RemoveNullCheckBeforeInstanceofASTVisitor());
	}

	@Test
	public void visit_infixExpressionAndNullCheck_shouldReplace() throws Exception {
		assertChange(//
				"String value = \"\"; if (value != null && value instanceof String) { }", //
				"String value = \"\"; if (value instanceof String) { }");
	}

	@Test
	public void visit_nullLiteralInLHS_shouldReplace() throws Exception {
		assertChange(//
				"String value = \"\"; if (null != value && value instanceof String) { }", //
				"String value = \"\"; if (value instanceof String) { }");
	}

	@Test
	public void visit_initializationExpression_shouldReplace() throws Exception {
		assertChange(//
				"String value = \"\"; boolean b = value != null && value instanceof String;",
				"String value = \"\"; boolean b = value instanceof String;");
	}

	@Test
	public void visit_multipleOperands_shouldReplace() throws Exception {
		assertChange(//
				"String value = \"\"; if (value != null && value instanceof String && value.isEmpty()) { }",
				"String value = \"\"; if (value instanceof String && value.isEmpty()) { }");
	}

	@Test
	public void visit_infixExpressionOrNullCheck_shouldReplace() throws Exception {
		assertChange(//
				"String value = \"\"; if (value == null || !(value instanceof String)) { }",
				"String value = \"\"; if (!(value instanceof String)) { }");
	}

	@Test
	public void visit_multipleOperandsOr_shouldReplace() throws Exception {
		assertChange(//
				"String value = \"\"; if (value == null || !(value instanceof String) || value.isEmpty()) { }",
				"String value = \"\"; if (!(value instanceof String) || value.isEmpty()) { }");
	}

	@Test
	public void visit_nestedParenthesizedExpression_shouldReplace() throws Exception {
		assertChange(//
				"String value = \"\"; if (value == null || !(((value instanceof String)))) { }",
				"String value = \"\"; if (!(((value instanceof String)))) { }");
	}

	@Test
	public void visit_missingNullCheck_shouldNotReplace() throws Exception {
		assertNoChange("String value = \"\"; if (value instanceof String) { }");
	}

	@Test
	public void visit_missingNullCheckNegatedExpression_shouldNotReplace() throws Exception {
		assertNoChange("String value = \"\"; if (!(value instanceof String)) { }");
	}

	@Test
	public void visit_mismatchNotEqualsNullCheckOperator_shouldNotReplace() throws Exception {
		assertNoChange("String value = \"\"; if (value == null && value instanceof String) { }");
	}

	@Test
	public void visit_mismatchEqualsNullCheckOperator_shouldNotReplace() throws Exception {
		assertNoChange("String value = \"\"; if (value != null || !(value instanceof String)) { }");
	}

	@Test
	public void visit_missingPrefixOperator_shouldNotReplace() throws Exception {
		assertNoChange("String value = \"\"; if (value == null || (value instanceof String)) { }");
	}

	@Test
	public void visit_missingPrefixOperatorNestedParenthesizedExpression_shouldNotReplace() throws Exception {
		assertNoChange("String value = \"\"; if (value == null || (((((value instanceof String)))))) { }");
	}

	@Test
	public void visit_missingSimpleNameInInstanceof_shouldNotReplace() throws Exception {
		assertNoChange("String block = \" \"; if(block != null && block.substring(0) instanceof String) {}");
	}

	@Test
	public void visit_mismatchingInfixCondition_shouldNotReplace() throws Exception {
		assertNoChange("String value = \"\"; if (value != null || value instanceof String) { }");
	}

	@Test
	public void visit_mismatchingNullCheckAndInstanceofLHSs_shouldNotReplace() throws Exception {
		assertNoChange("" +
				"BufferedReader r;\n" + 
				"String line;\n" +
				"while ((line = r.readLine()) != null && line instanceof String) {}");
	}

	@Test
	public void visit_missingNullLiteral_shouldNotReplace() throws Exception {
		assertNoChange("String value, value2 = \"\"; if (value != value2 && value instanceof String) { }");
	}

}
