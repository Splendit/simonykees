package at.splendit.simonykees.sample.postRule.removeNewString;

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

	public String testNestedNewStrings(String input) {
		return input;
	}
}
