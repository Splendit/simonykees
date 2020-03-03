package eu.jsparrow.sample.postRule.allRules;

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

		string = input + string // don't break the semicolon
		/* */;

		// 3. --------------- --------------- --------------- ---------------

		final int b = "".compareTo(input.concat("abc" // don't break the
														// semicolon
		));

		final String c = input.concat("abc" // don't break the semicolon
		) + "";

		// 4. --------------- --------------- --------------- ---------------

		return input // I don't want to break anything
			. // save me
			concat("abc" // don't break the semicolon
			);
	}

	public String testConcatWithVariable(String input, String param) {
		return input + param;
	}

	public String testConcatOnlyLiterals() {
		return "abc" + "def";
	}

	public String testConcatRecursionWithLiteral(String input) {
		return new StringBuilder().append(input)
			.append("abc")
			.append("def")
			.toString();
	}

	public String testConcatRecursionWithParam(String input, String param) {
		return new StringBuilder().append(input)
			.append(param)
			.append(param)
			.toString();
	}

	public String testConcatRecursionWithParamAndCast(String input, String param) {
		/* save me */
		return new StringBuilder().append(input)
			.append(param)
			.append(param)
			.toString();
	}

	public String testConcatMixedWithPlus(String input, String param) {
		return new StringBuilder().append(input)
			.append(param)
			.append(param)
			.append(input)
			.append(param)
			.append(param)
			.toString();
	}

	public String testConcatWithToString(String input, String param) {
		return input + param;
	}

	public String testConcatChain(String input) {
		return new StringBuilder().append(input)
			.append("abc")
			.append("cde")
			.append("fgh")
			.append("hij")
			.toString();
	}

	public String testConcatWithResultOfMethodCall(String input) {
		return input + sampleMethod();
	}

	public String testConcatWithResultOfMethodCallWithParams(String input, String param) {
		return new StringBuilder().append(input)
			.append(sampleMethod(param))
			.append(sampleMethod())
			.toString();
	}

	public String testConcatWithNumber(String input) {
		final Number number = new BigDecimal("10.00");
		return input + number.toString();
	}

	public String testConcatWithStreamResult(String input) {
		final List<String> values = Arrays.asList("val1", "val2", input);
		return new StringBuilder().append(input)
			.append(values.stream()
				.filter(s -> s.equals(input))
				.collect(Collectors.joining(",")))
			.append(values.stream()
				.collect(Collectors.joining(";")))
			.toString();
	}

	public String testConcatEmptyString(String input) {
		return input + "";
	}

	public String testConcatEmptyCharacter(String input) {
		return new StringBuilder().append(input)
			.append('c')
			.append("")
			.toString();
	}

	public String testConcatDeepNestedConcats(String input, String param) {
		return new StringBuilder().append(input)
			.append(param)
			.append("a.concat()")
			.append("b")
			.append("\"c")
			.append("i")
			.append(param)
			.append("e")
			.append("d")
			.append("h")
			.append("f")
			.append("g")
			.toString();
	}

	public String testConcatInsideCodeBlock(String input, String parameter) {
		String result = "";
		final List<String> values = Arrays.asList("val1", "val2", input);

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
			result = new StringBuilder().append(result)
				.append(key)
				.append(",")
				.toString();
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
		final boolean startsWitParam = input.startsWith((param + "a"), 0);
		return input + Boolean.toString(startsWitParam);
	}

	public String testConcatRecursionWithLiteral_saveComments(String input) {
		// save comment 1
		return new StringBuilder().append(input)
			.append("abc")
			.append("def")
			.toString();
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
