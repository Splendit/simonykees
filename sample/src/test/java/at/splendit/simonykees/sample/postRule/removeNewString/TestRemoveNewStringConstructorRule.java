package at.splendit.simonykees.sample.postRule.removeNewString;

@SuppressWarnings("nls")
public class TestRemoveNewStringConstructorRule {

	public String testNewEmptyStrig() {
		return "";
	}

	public String testNewStringOfLiteral() {
		return "StringLiteral";
	}
	
	public String testNewStringOfOtherString(String s) {
		return s;
	}
	
	public String testNewStringOnNonStringElement(StringBuilder sb) {
		return new String(sb);
	}
}
