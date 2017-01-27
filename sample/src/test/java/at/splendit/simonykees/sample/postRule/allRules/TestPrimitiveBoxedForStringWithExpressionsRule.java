package at.splendit.simonykees.sample.postRule.allRules;

import org.apache.commons.lang3.StringUtils;

public class TestPrimitiveBoxedForStringWithExpressionsRule {

	private int intSampleMethod() {
		return 1;
	}

	private int intSampleMethod(String intVal) {
		return Integer.valueOf(intVal);
	}

	public String testIntegerValueOfExpressionToString(int input) {
		return Integer.toString(input + 3);
	}

	public String testIntegerValueOfExpressionWithMethodInvoke(int input) {
		return Integer.toString(intSampleMethod() + input);
	}

	public String testIntegerValueOfMixedTypeExpression(int input) {
		return Integer.valueOf(input + Integer.valueOf(1) + intSampleMethod()).toString();
	}

	public String testIntegerValueOfCascadeExpressions(int input) {
		return Integer.toString(input + intSampleMethod()) + Integer.toString(input + "abc".length());
	}

	public String testIntegerValueOfInIfCondition(int input) {
		String result = "";
		if (!StringUtils.isEmpty(Integer.toString(input)) || Integer.toString(5).length() == 1) {
			result = Integer.toString(input);
		}
		return result;
	}

	public String testIntegerBoxingOnExpression(int input) {
		String result = Integer.toString(5 + input + intSampleMethod("2"))
				+ Integer.toString(input + intSampleMethod());
		return result;
	}

	public String testNestedIntegerBoxing(int input) {
		String val = Integer.valueOf(Integer.valueOf(input) + 1).toString();
		return val;
	}

	public String testMethodInvocationIntegerBoxing(int input) {
		String val = Integer.toString(intSampleMethod(Integer.toString(3)) + 4);
		return val;
	}

	public String testLiteralConcatWithExpression(int input) {
		return Integer.toString((input + 1 + intSampleMethod("4")));
	}
}
