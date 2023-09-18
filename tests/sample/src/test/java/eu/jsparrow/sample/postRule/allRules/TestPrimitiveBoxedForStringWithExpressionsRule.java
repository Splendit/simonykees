package eu.jsparrow.sample.postRule.allRules;

import org.apache.commons.lang3.StringUtils;

public class TestPrimitiveBoxedForStringWithExpressionsRule {

	private int intSampleMethod() {
		return 1;
	}

	private int intSampleMethod(String intVal) {
		return Integer.valueOf(intVal);
	}

	public String testIntegerValueOfExpressionToString(int input) {
		// test
		return Integer.toString(input + 3);
	}

	public String testIntegerValueOfExpressionWithMethodInvoke(int input) {
		// test
		/* invocation comment */
		/* expression comment */
		return Integer.toString(/* internal arg comment */ intSampleMethod() + input);
	}

	public String test_lineCommentFollowingExpression(int input) {

		// test
		return Integer.toString(intSampleMethod() + input);
	}

	public String testIntegerValueOfMixedTypeExpression(int input) {
		return Integer.toString(input + Integer.valueOf(1) + intSampleMethod());
	}

	public String testIntegerValueOfCascadeExpressions(int input) {
		return Integer.toString(input + intSampleMethod()) + Integer.toString(input + "abc".length());
	}

	public String testIntegerValueOfInIfCondition(int input) {
		String result = "";
		if (!StringUtils.isEmpty(Integer.toString(input)) || Integer.toString(5)
			.length() == 1) {
			result = Integer.toString(input);
		}
		return result;
	}

	public String testIntegerBoxingOnExpression(int input) {

		/* declaration comment */
		/* toString expression comment */
		final String savingCmments = /* leading comment */ Integer
			.toString(/* ctor arg comment */ 5 + input
					+ intSampleMethod("2")) /* trailing comment */ ;

		return Integer.toString(5 + input + intSampleMethod("2")) + Integer.toString(input + intSampleMethod());
	}

	public String testNestedIntegerBoxing(int input) {
		return Integer.toString(Integer.valueOf(input) + 1);
	}

	public String testMethodInvocationIntegerBoxing(int input) {
		return Integer.toString(intSampleMethod(Integer.toString(3)) + 4);
	}

	public String testLiteralConcatWithExpression(int input) {
		return Integer.toString((input + 1 + intSampleMethod("4")));
	}
}
