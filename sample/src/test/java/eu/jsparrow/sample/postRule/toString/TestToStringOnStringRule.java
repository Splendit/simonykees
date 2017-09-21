package eu.jsparrow.sample.postRule.toString;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("nls")
public class TestToStringOnStringRule {
	
	private String sampleMethod() {
		return "sample-method";
	}
	
	private Number numberSampleMethod() {
		return new Integer("100");
	}
	
	private String sampleMethod(String param) {
		return param + "sample-method";
	}

	public String testToStringOnStringLiteral() {

		return "anStringLiteral";

	}

	public String testToStringOnStringVariable() {
		String s = "theStringS";

		return s;

	}

	public String testToStringOnStringFunctionThatReturnsString() {

		return StringUtils.abbreviate("makeMeShorter", 4);

	}

	public String testToStringOnParenthesizeString(String s) {

		return s;

	}

	public String testToStringOnParenthesizePlusString(String s) {

		return s + "abc";

	}
	
	public String testToStringOnMethodInvocation(String s) {
		return sampleMethod();
	}
	
	public String testToStringChain(String input) {
		return input;
	}
	
	public String testToStringToLowerCaseChain(String input) {
		return input.toLowerCase();
	}
	
	public String testToStringOnOtherTypes(String input) {
		String sampleString = numberSampleMethod().toString();
		return sampleString + numberSampleMethod().toString();
	}
	
	public String testDiscardToStringOnString(String input) {
		input.toString();
		return input;
	}
	
	public String testToStringInNestedConcatOperations(String input) {
		String val = input + ("some-other-nasty-string") + "abc" + 'c';
		return val;
	}
	
	public String testToStringInLambdaExpressionBody(String input) {
		List<String> stringList = Arrays.asList(input, "foo");
		String result = 
				stringList
				.stream()
				.map(s -> s + ";")
				.collect(Collectors.joining(","));
		return result;
	}
	
	public String testToStringOnMethodInvocationParameters(String input) {
		String result = sampleMethod(input);
		return result;
	}
	
	public String testRemoveToStringInCodeBlock(String input) {
		String result = "";
		if(!input.isEmpty()) {
			try {
				result = input + "-";
			} finally {
				result = result + ";";
			}
		}
		return result;
	}
	
	public String testRemoveToStringOnIfCondition(String input) {
		String result = "";
		if(!input.isEmpty()) {
			result = "nonEmpty:" + input;
		}
		return result;
	}
	
	public String testRemoveToStringOnForCondition(String input) {
		String result = "";
		if(!input.isEmpty()) {
			result = "nonEmpty:" + input;
			for(int i = 0; sampleMethod(input).length() == 0 || i < sampleMethod(input).length(); i++) {
				result = "empty:" + input;				
			}
		}
		return result;
	}

	public String testRemoveToStringOnWhileCondition(String input) {
		String result = "";
		if(!input.isEmpty()) {
			result = "nonEmpty:" + input;
			while (input.length() == 0) {
				result = "empty:" + input;				
			}
		}
		return result;
	}
	
	public String testChainMethodInvocatioonToString(String input) {
		String className = this.getClass().getName();
		return input + className;
	}
}
