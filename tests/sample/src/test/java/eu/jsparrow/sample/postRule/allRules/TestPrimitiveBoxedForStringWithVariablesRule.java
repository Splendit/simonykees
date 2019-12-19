package eu.jsparrow.sample.postRule.allRules;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("nls")
public class TestPrimitiveBoxedForStringWithVariablesRule {

	public String testIntegerLiteralConcat(int input) {
		return Integer.toString(input);
	}

	public String testIntegerNewLiteralTostring(int input) {
		return Integer.toString(input);
	}

	public String testIntegerValueOfToString(int input) {
		return Integer.toString(input);
	}

	public String testLongLiteralConcat(long input) {
		return Long.toString(input);
	}

	public String testLongLongNewLiteralTostring(long input) {
		return Long.toString(input);
	}

	public String testLongIntegerNewLiteralTostring(int input) {
		return Long.toString(input);
	}

	public String testLongLongValueOfToString(long input) {
		return Long.toString(input);
	}

	public String testLongIntegerValueOfToString(int input) {
		return Long.toString(input);
	}

	public String testFloatLiteralConcat(float input) {
		return Float.toString(input);
	}

	public String testFloatNewLiteralTostring(float input) {
		return Float.toString(input);
	}

	public String testFloatDoubleNewLiteralTostring(double input) {
		return new Float(input).toString();
	}

	public String testFloatLongNewLiteralTostring(long input) {
		return Float.toString(input);
	}

	public String testFloatIntegerNewLiteralTostring(int input) {
		return Float.toString(input);
	}

	public String testFloatValueOfToString(float input) {
		return Float.toString(input);
	}

	public String testFloatLongValueOfToString(long input) {
		return Float.toString(input);
	}

	public String testFloatIntegerValueOfToString(int input) {
		return Float.toString(input);
	}

	public String testDoubleLiteralConcat(double input) {
		return Double.toString(input);
	}

	public String testDoubleNewLiteralTostring(double input) {
		return Double.toString(input);
	}

	public String testDoubleFloatNewLiteralTostring(float input) {
		return Double.toString(input);
	}

	public String testDoubleLongNewLiteralTostring(long input) {
		return Double.toString(input);
	}

	public String testDoubleIntegerNewLiteralTostring(int input) {
		return Double.toString(input);
	}

	public String testDoubleValueOfToString(double input) {
		return Double.toString(input);
	}

	public String testDoubleFloatValueOfToString(float input) {
		return Double.toString(input);
	}

	public String testDoubleLongValueOfToString(long input) {
		return Double.toString(input);
	}

	public String testDoubleIntegerValueOfToString(int input) {
		return Double.toString(input);
	}

	public String testIntegertoStringWithParameters(int input) {
		return Integer.valueOf(input)
			.toString(4);
	}

	public String test(Integer input) {
		String result = "";

		if (!StringUtils.isEmpty((Integer.toString(input)))) {
			result = Integer.toString(intSampleMethod("5") + Integer.valueOf(3) + input);
			final Integer res = input + Integer.valueOf(1);
			result = result + res.toString();
		}

		return result;
	}

	private Integer intSampleMethod(String returnME) {
		return Integer.valueOf(returnME);
	}
}
