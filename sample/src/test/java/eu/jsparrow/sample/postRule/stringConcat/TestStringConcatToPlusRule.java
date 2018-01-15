package eu.jsparrow.sample.postRule.stringConcat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("nls")
public class TestStringConcatToPlusRule {

	private static final String STATIC_VALUE = "static-value";

	private String sampleMethod() {
		return "method-invocation-result";
	}

	private String sampleMethod(String param) {
		return "method-invocation-result" + param;
	}

	public String testConcatWithLiteral(String input) {
		// save me
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
		/* save me */
		return input + (String) param + param;
	}

	public String testConcatMixedWithPlus(String input, String param) {
		return input + param + param + input + param + param;
	}

	public String testConcatWithToString(String input, String param) {
		return (input + param).toString();
	}

	public String testConcatChain(String input) {
		return input + "abc" + "cde" + "fgh" + "hij";
	}

	public String testConcatWithResultOfMethodCall(String input) {
		return input + sampleMethod();
	}

	public String testConcatWithResultOfMethodCallWithParams(String input, String param) {
		return input + sampleMethod(param) + sampleMethod();
	}

	public String testConcatWithNumber(String input) {
		Number number = new BigDecimal("10.00");
		return input + number.toString();
	}

	public String testConcatWithStreamResult(String input) {
		List<String> values = Arrays.asList("val1", "val2", input);
		return input + values.stream().filter(s -> s.equals(input)).collect(Collectors.joining(",")) + values.stream().collect(Collectors.joining(";"));
	}

	public String testConcatEmptyString(String input) {
		return input + "";
	}

	public String testConcatEmptyCharacter(String input) {
		return input + 'c' + "";
	}

	public String testConcatDeepNestedConcats(String input, String param) {
		return input + param + "a.concat()" + "b" + "\"c" + "i"
				+ param + "e" + "d" + "h" + "f" + "g";
	}

	public String testConcatInsideCodeBlock(String input, String parameter) {
		String result = "";
		List<String> values = Arrays.asList("val1", "val2", input);

		if ("".equals(result)) {
			result = result + input;
		}

		switch (result) {
		case "some-val":
			result = result + parameter;
			break;
		default:
			result = result + parameter;
		}
		result = result + ";";

		for (String key : values) {
			result = result + key + ",";
		}

		result = result + ";";

		return result;
	}

	public String testConcatWithStaticField(String input) {
		return input + STATIC_VALUE;
	}

	public String testDiscardConcatResult(String input, String parameter) {
		input.concat(parameter);
		return input + parameter;
	}

	public String testConcatInMethodInvocationParam(String input, String param) {
		boolean startsWitParam = input.startsWith((param + "a"), 0);
		return input + Boolean.toString(startsWitParam);
	}
	
	public String testConcatRecursionWithLiteral_saveComments(String input) {
		// save comment 1
		// save comment 2
		return input + "abc" + "def";
	}
}
