package at.splendit.simonykees.sample.postRule.allRules;

@SuppressWarnings("nls")
public class TestStringConcatToPlusRule {

	public String testConcatWithLiteral(String input) {
		return input + "abc";
	}

	public String testConcatWithVariable(String input, String param) {
		return input + param;
	}

	public String testConcatOnlyLiterals() {
		return "abc" + "def";
	}

	public String testConcatRecursionWithLiteral(String input) {
		return input + "abc" + "def";
	}

	public String testConcatRecursionWithParam(String input, String param) {
		return input + param + param;
	}

	public String testConcatRecursionWithParamAndCast(String input, String param) {
		return input + (String) param + param;
	}

	public String testConcatMixedWithPlus(String input, String param) {
		return input + param + param + input + param + param;
	}

	public String testConcatWithToString(String input, String param) {
		return input + param;
	}
}
