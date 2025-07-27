package eu.jsparrow.sample.postRule.allRules;

import java.math.BigDecimal;
import java.util.Collections;
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

	public String testNewStringOfLiteralWithComments() {
		/* save comments */
		return "StringLiteral";
	}

	public String testNewStringOfLiteral_lineComments() {
		// I don't want to break anything
		return "StringLiteral";
	}

	public String testNewStringOfOtherString(String s) {
		return s;
	}

	public String testNewStringOfOtherString_lineComments(String s) {
		// I don't want to break anything
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
		final byte[] bytes = input.getBytes();
		return new String(bytes);
	}

	public String testNestedNewStringsAndConcat(String input) {
		return new StringBuilder().append(input)
			.append("-")
			.append(input)
			.append("-")
			.append("val")
			.append("val")
			.toString();
	}

	public String testNestedNewStrings(String input) {
		return input;
	}

	public String testDeepNestedNewStringCtor(String input) {
		return sampleMethod(input);
	}

	public String testCharArrayInput(String input) {
		final char[] charArray = input.toCharArray();
		return new String(charArray);
	}

	public String testCharArrayInputOffest(String input) {
		final char[] charArray = input.toCharArray();
		return new String(charArray, 0, 1);
	}

	public String testStringBuffer(String input) {
		StringBuilder buffer = new StringBuilder(input);
		return new String(buffer);
	}

	public String testDiscardCtorResult(String input) {
		new String(input);
		return input;
	}

	public String testConvertInsideBlock(String input) {
		String result = "";
		final boolean condition = !StringUtils.isEmpty(input) && StringUtils.isEmpty(result);
		if (condition) {
			result = input;
		}
		return result;
	}

	public String testConvertInputParameter(String input) {
		final BigDecimal number = new BigDecimal("10.05");
		Integer.valueOf("123");
		return input + number;
	}

	public String testConvertInLambdaExpressionBody(String input) {
		final List<String> list = Collections.singletonList(input);
		return list.stream()
			.map(t -> t)
			.collect(Collectors.joining());
	}
}
