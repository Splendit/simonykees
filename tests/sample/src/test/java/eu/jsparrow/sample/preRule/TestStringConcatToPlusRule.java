package eu.jsparrow.sample.preRule;

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
		
		// 1. --------------- --------------- --------------- ---------------
		
		String string = input.concat("abc" // don't break the semicolon
				/* */
				// c2
						);
		
		// 2. --------------- --------------- --------------- ---------------
		
		string = input.
				concat(string // don't break the semicolon
						/* */
						);
		
		// 3. --------------- --------------- --------------- ---------------
		
		int b = "".compareTo(input.concat("abc" // don't break the semicolon
						));
		
		String c = input.concat("abc" // don't break the semicolon
				) + "";
		
		// 4. --------------- --------------- --------------- ---------------
		
		return input // I don't want to break anything
				. // save me
				concat("abc" // don't break the semicolon
						);
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
		return input.concat((String) param.concat(param))/* save me */;
	}

	public String testConcatMixedWithPlus(String input, String param) {
		return input.concat(param.concat(param)) + input.concat(param.concat(param));
	}

	public String testConcatWithToString(String input, String param) {
		return input.concat(param).toString();
	}

	public String testConcatChain(String input) {
		return input.concat("abc").concat("cde").concat("fgh".concat("hij"));
	}

	public String testConcatWithResultOfMethodCall(String input) {
		return input.concat(sampleMethod());
	}

	public String testConcatWithResultOfMethodCallWithParams(String input, String param) {
		return input.concat(sampleMethod(param) + sampleMethod());
	}

	public String testConcatWithNumber(String input) {
		Number number = new BigDecimal("10.00");
		return input.concat(number.toString());
	}

	public String testConcatWithStreamResult(String input) {
		List<String> values = Arrays.asList("val1", "val2", input);
		return input.concat(values.stream().filter(s -> s.equals(input)).collect(Collectors.joining(","))
				.concat(values.stream().collect(Collectors.joining(";"))));
	}

	public String testConcatEmptyString(String input) {
		return input.concat("");
	}

	public String testConcatEmptyCharacter(String input) {
		return input.concat('c' + "");
	}

	public String testConcatDeepNestedConcats(String input, String param) {
		return input.concat(param.concat("a.concat()".concat("b".concat("\"c".concat("i"))))
				+ param.concat("e".concat("d") + "h".concat("f".concat("g"))));
	}

	public String testConcatInsideCodeBlock(String input, String parameter) {
		String result = "";
		List<String> values = Arrays.asList("val1", "val2", input);

		if ("".equals(result)) {
			result = result.concat(input);
		}

		switch (result) {
		case "some-val":
			result = result.concat(parameter);
			break;
		default:
			result = result.concat(parameter);
		}
		result = result.concat(";");

		for (String key : values) {
			result = result.concat(key.concat(","));
		}

		result = result.concat(";");

		return result;
	}

	public String testConcatWithStaticField(String input) {
		return input.concat(STATIC_VALUE);
	}

	public String testDiscardConcatResult(String input, String parameter) {
		input.concat(parameter);
		return input.concat(parameter);
	}

	public String testConcatInMethodInvocationParam(String input, String param) {
		boolean startsWitParam = input.startsWith(param.concat("a"), 0);
		return input.concat(Boolean.toString(startsWitParam));
	}
	
	public String testConcatRecursionWithLiteral_saveComments(String input) {
		return input.concat( // save comment 1
				"abc".concat( // save comment 2
						"def"));
	}
	
	public void test_missingExpression_shouldNotTransform() {
		/*
		 * SIM-1350
		 */
		
		concat("fake", "news");
	}
	
	private void concat(String fake, String concat) {
		fake.concat(concat);
	}
}
