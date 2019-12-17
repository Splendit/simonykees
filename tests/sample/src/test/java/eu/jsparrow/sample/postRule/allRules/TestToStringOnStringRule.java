package eu.jsparrow.sample.postRule.allRules;

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
		return Integer.valueOf("100");
	}

	private String sampleMethod(String param) {
		return param + "sample-method";
	}

	public String testToStringOnStringLiteral() {

		return "anStringLiteral";

	}

	public String testToStringOnStringVariable() {
		final String s = "theStringS";

		return s;

	}

	public String testToStringOnStringVariable_saveComments() {
		String s = "theStringS";
		/* save me */
		// save me
		String uselsessToString = s;
		/* save me */
		uselsessToString = s;
		uselsessToString = // save me
				s;
		/* save me */
		s = StringUtils.substring(s, 0);

		s = StringUtils.substring(s, 0)// save me
			.toString();

		final String input = "noInput";
		s = new StringBuilder().append(StringUtils.substring(s, 0) // save me
			.toString())
			.append(input)
			.append("some-other-nasty-string" // skip me
				.toString())
			.append("abc" // you dont want to break the code
				.toString())
			.append('c')
			.toString();

		/* leading comment */
		/* invocation comment */
		/* trailing comment */
		uselsessToString = "uselessToString"/* expression comment */;
		/* save me */
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
		return StringUtils.lowerCase(input);
	}

	public String testToStringOnOtherTypes(String input) {
		final String sampleString = numberSampleMethod().toString();
		return sampleString + numberSampleMethod().toString();
	}

	public String testDiscardToStringOnString(String input) {
		input.toString();
		return input;
	}

	public String testToStringInNestedConcatOperations(String input) {
		final String val = new StringBuilder().append(input)
			.append("some-other-nasty-string")
			.append("abc")
			.append('c')
			.toString();
		return val;
	}

	public String testToStringInLambdaExpressionBody(String input) {
		final List<String> stringList = Arrays.asList(input, "foo");
		final String result = stringList.stream()
			.map(s -> s + ";")
			.collect(Collectors.joining(","));
		return result;
	}

	public String testToStringOnMethodInvocationParameters(String input) {
		final String result = sampleMethod(input);
		return result;
	}

	public String testRemoveToStringInCodeBlock(String input) {
		String result = "";
		if (!StringUtils.isEmpty(input)) {
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
		if (!StringUtils.isEmpty(input)) {
			result = "nonEmpty:" + input;
		}
		return result;
	}

	public String testRemoveToStringOnForCondition(String input) {
		String result = "";
		if (!StringUtils.isEmpty(input)) {
			result = "nonEmpty:" + input;
			for (int i = 0; sampleMethod(input).isEmpty() || i < sampleMethod(input).length(); i++) {
				result = "empty:" + input;
			}
		}
		return result;
	}

	public String testRemoveToStringOnWhileCondition(String input) {
		String result = "";
		if (!StringUtils.isEmpty(input)) {
			result = "nonEmpty:" + input;
			while (input.isEmpty()) {
				result = "empty:" + input;
			}
		}
		return result;
	}

	public String testChainMethodInvocatioonToString(String input) {
		final String className = this.getClass()
			.getName();
		return input + className;
	}
}
