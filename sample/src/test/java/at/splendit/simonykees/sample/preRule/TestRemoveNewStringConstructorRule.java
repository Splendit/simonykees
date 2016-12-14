package at.splendit.simonykees.sample.preRule;

@SuppressWarnings("nls")
public class TestRemoveNewStringConstructorRule {

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
}
