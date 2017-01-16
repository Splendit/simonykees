package at.splendit.simonykees.sample.postRule.allRules;

@SuppressWarnings("nls")
public class TestRemoveNewStringConstructorRule {

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
}
