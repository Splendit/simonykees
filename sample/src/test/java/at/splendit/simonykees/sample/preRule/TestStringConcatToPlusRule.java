package at.splendit.simonykees.sample.preRule;

@SuppressWarnings("nls")
public class TestStringConcatToPlusRule {

	public String testConcatWithLiteral(String input) {
		return input.concat("abc");
	}

	public String testConcatWithVariable(String input, String param) {
		return input.concat(param);
	}

	public String testConcatOnlyLiterals() {
		return "abc".concat("def");
	}

	public String testConcatRecursionWithLiteral(String input) {
		return input.concat("abc".concat("def"));
	}

	public String testConcatRecursionWithParam(String input, String param) {
		return input.concat(param.concat(param));
	}

	public String testConcatRecursionWithParamAndCast(String input, String param) {
		return input.concat((String) param.concat(param));
	}

	public String testConcatMixedWithPlus(String input, String param) {
		return input.concat(param.concat(param)) + input.concat(param.concat(param));
	}

	public String testConcatWithToString(String input, String param) {
		return input.concat(param).toString();
	}
}
