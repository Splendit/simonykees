package at.splendit.simonykees.sample.postRule.allRules;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("nls")
public class TestRemoveNewStringConstructorRule {

	private String sampleMethod() {
		return "sample-method";
	}

	private String sampleMethod(String input) {
		return "sample-method-" + input;
	}

	public String testNewEmptyStrig() {
		return "";
	}

	public String testNewStringOfLiteral() {
		return "StringLiteral";
	}

	public String testNewStringOfLiteralWithParentheses() {
		return "StringLiteral";
	}

	public String testNewStringOfOtherString(String s) {
		return s;
	}

	public String testNewStringOnNonStringElement(StringBuilder sb) {
		return new String(sb);
	}

	public String testNewStringOnMethodInvocation() {
		return sampleMethod();
	}

	public String testNewStringOnMethodInvocationWithParams(String input) {
		return sampleMethod(input);
	}

	public String testNewStringOnByteArray(String input) {
		byte[] bytes = input.getBytes();
		return new String(bytes);
	}

	public String testNestedNewStringsAndConcat(String input) {
		return input + "-" + input + "-" + "val" + "val";
	}

	public String testNestedNewStrings(String input) {
		return input;
	}

	public String testDeepNestedNewStringCtor(String input) {
		return sampleMethod(input);
	}

	public String testCharArrayInput(String input) {
		char[] charArray = input.toCharArray();
		String result = new String(charArray);
		return result;
	}

	public String testCharArrayInputOffest(String input) {
		char[] charArray = input.toCharArray();
		String result = new String(charArray, 0, 1);
		return result;
	}

	public String testStringBuffer(String input) {
		StringBuffer buffer = new StringBuffer(input);
		return new String(buffer);
	}

	public String testDiscardCtorResult(String input) {
		new String(input);
		return input;
	}

	public String testConvertInsideBlock(String input) {
		String result = "";
		if (!StringUtils.isEmpty(input)) {
			if (StringUtils.isEmpty(result)) {
				result = input;
			}
		}
		return result;
	}

	public String testConvertInputParameter(String input) {
		BigDecimal number = new BigDecimal("10.05");
		Integer.valueOf("123");
		return input + number;
	}

	public String testConvertInLambdaExpressionBody(String input) {
		List<String> list = Arrays.asList(input);
		String result = list.stream().map(t -> t).collect(Collectors.joining());
		return result;
	}
}
