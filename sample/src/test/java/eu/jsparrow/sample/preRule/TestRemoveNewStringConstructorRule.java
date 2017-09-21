package eu.jsparrow.sample.preRule;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("nls")
public class TestRemoveNewStringConstructorRule {
	
	private String sampleMethod() {
		return "sample-method";
	}

	private String sampleMethod(String input) {
		return "sample-method-" + input;
	}

	public String testNewEmptyStrig() {
		return new String();
	}

	public String testNewStringOfLiteral() {
		return new String("StringLiteral");
	}

	public String testNewStringOfLiteralWithParentheses() {
		return new String(("StringLiteral"));
	}

	public String testNewStringOfOtherString(String s) {
		return new String(s);
	}

	public String testNewStringOnNonStringElement(StringBuilder sb) {
		return new String(sb);
	}
	
	public String testNewStringOnMethodInvocation() {
		return new String(sampleMethod());
	}
	
	public String testNewStringOnMethodInvocationWithParams(String input) {
		return new String(sampleMethod(input));
	}
	
	public String testNewStringOnByteArray(String input) {
		byte [] bytes = input.getBytes();
		return new String(bytes);
	}
	
	public String testNestedNewStringsAndConcat(String input) {
		return new String(new String(input)+ "-" + input + "-" + new String("val" + new String("val")));
	}
	
	public String testNestedNewStrings(String input) {
		return new String(new String(input));
	}
	
	public String testDeepNestedNewStringCtor(String input) {
		return new String(new String(new String(sampleMethod(input))));
	}
	
	public String testCharArrayInput(String input) {
		char []charArray = input.toCharArray();
		String result = new String(charArray);
		return result;
	}
	
	public String testCharArrayInputOffest(String input) {
		char []charArray = input.toCharArray();
		String result = new String(charArray, 0, 1);
		return result;
	}
	
	public String testStringBuffer(String input) {
		StringBuffer buffer = new StringBuffer(input);
		return new String (buffer);
	}
	
	public String testDiscardCtorResult(String input) {
		new String(input);
		return input;
	}
	
	public String testConvertInsideBlock(String input) {
		String result = "";
		if(!input.isEmpty()) {
			if(result.isEmpty()) {
				result = new String(input);
			}
		}
		return result;
	}
	
	public String testConvertInputParameter(String input) {
		BigDecimal number = new BigDecimal(new String("10.05"));
		Integer.valueOf(new String("123"));
		return input + number;
	}
	
	public String testConvertInLambdaExpressionBody(String input) {
		List<String> list = Arrays.asList(input);
		String result = new String(
				list.stream()
				.map(t -> new String(t))
				.collect(Collectors.joining()));
		return result;
	}
}
