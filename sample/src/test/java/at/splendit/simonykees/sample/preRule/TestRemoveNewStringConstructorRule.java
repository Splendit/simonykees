package at.splendit.simonykees.sample.preRule;

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
	
	public String testNestedNewStrings(String input) {
		return new String(new String(input));
	}
}
