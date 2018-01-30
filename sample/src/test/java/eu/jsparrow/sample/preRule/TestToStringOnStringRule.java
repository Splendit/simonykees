package eu.jsparrow.sample.preRule;

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

		return "anStringLiteral".toString();

	}

	public String testToStringOnStringVariable() {
		String s = "theStringS";

		return s.toString();

	}
	
	public String testToStringOnStringVariable_saveComments() {
		String s = "theStringS";
		// save me
		String uselsessToString =  /* save me */ s.toString();
		uselsessToString = /* save me */ s.toString();
		uselsessToString = // save me
				s.toString();
		s = s.substring(0). /* save me */ toString();
		return /* save me */s.toString();

	}

	public String testToStringOnStringFunctionThatReturnsString() {

		return StringUtils.abbreviate("makeMeShorter", 4).toString();

	}

	public String testToStringOnParenthesizeString(String s) {

		return (s).toString();

	}

	public String testToStringOnParenthesizePlusString(String s) {

		return (s + "abc").toString();

	}
	
	public String testToStringOnMethodInvocation(String s) {
		return sampleMethod().toString();
	}
	
	public String testToStringChain(String input) {
		return input.toString().toString();
	}
	
	public String testToStringToLowerCaseChain(String input) {
		return input.toString().toLowerCase();
	}
	
	public String testToStringOnOtherTypes(String input) {
		String sampleString = numberSampleMethod().toString();
		return sampleString.toString() + numberSampleMethod().toString();
	}
	
	public String testDiscardToStringOnString(String input) {
		input.toString();
		return input;
	}
	
	public String testToStringInNestedConcatOperations(String input) {
		String val = (input.toString() + ("some-other-nasty-string".toString()) + "abc".toString() + 'c').toString();
		return val;
	}
	
	public String testToStringInLambdaExpressionBody(String input) {
		List<String> stringList = Arrays.asList(input, "foo");
		String result = 
				stringList
				.stream()
				.map(s -> s.toString() + ";".toString())
				.collect(Collectors.joining(",".toString()));
		return result;
	}
	
	public String testToStringOnMethodInvocationParameters(String input) {
		String result = sampleMethod(input.toString());
		return result;
	}
	
	public String testRemoveToStringInCodeBlock(String input) {
		String result = "";
		if(!input.isEmpty()) {
			try {
				result = input.toString() + "-".toString();
			} finally {
				result = result + ";".toString();
			}
		}
		return result;
	}
	
	public String testRemoveToStringOnIfCondition(String input) {
		String result = "";
		if(!input.toString().isEmpty()) {
			result = "nonEmpty:" + input;
		}
		return result;
	}
	
	public String testRemoveToStringOnForCondition(String input) {
		String result = "";
		if(!input.toString().isEmpty()) {
			result = "nonEmpty:" + input;
			for(int i = 0; sampleMethod(input).toString().length() == 0 || i < sampleMethod(input).toString().length(); i++) {
				result = "empty:" + input;				
			}
		}
		return result;
	}

	public String testRemoveToStringOnWhileCondition(String input) {
		String result = "";
		if(!input.toString().isEmpty()) {
			result = "nonEmpty:" + input;
			while (input.toString().length() == 0) {
				result = "empty:" + input;				
			}
		}
		return result;
	}
	
	public String testChainMethodInvocatioonToString(String input) {
		String className = this.getClass().getName().toString();
		return input + className;
	}
}
