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
		return new StringBuilder().append("method-invocation-result")
			.append(param)
			.toString();
	}

	public String testConcatWithLiteral(String input) {

		// 1. --------------- --------------- --------------- ---------------

		String string = input.concat("abc" // don't break the semicolon
		/* */
		// c2
		);

		// 2. --------------- --------------- --------------- ---------------

		string = new StringBuilder().append(input)
			.append(string // don't break the semicolon
			/* */)
			.toString();

		// 3. --------------- --------------- --------------- ---------------

		int b = "".compareTo(input.concat("abc" // don't break the semicolon
		));

		String c = new StringBuilder().append(input.concat("abc" // don't break
																	// the
																	// semicolon
		))
			.append("")
			.toString();

		// 4. --------------- --------------- --------------- ---------------

		return input // I don't want to break anything
			. // save me
			concat("abc" // don't break the semicolon
			);
	}

	public String testConcatWithVariable(String input, String param) {
		return new StringBuilder().append(input)
			.append(param)
			.toString();
	}

	public String testConcatOnlyLiterals() {
		return new StringBuilder().append("abc")
			.append("def")
			.toString();
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
			.append((String) param)
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
		return new StringBuilder().append(input)
			.append(param)
			.toString();
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
		return new StringBuilder().append(input)
			.append(sampleMethod())
			.toString();
	}

	public String testConcatWithResultOfMethodCallWithParams(String input, String param) {
		return new StringBuilder().append(input)
			.append(sampleMethod(param))
			.append(sampleMethod())
			.toString();
	}

	public String testConcatWithNumber(String input) {
		Number number = new BigDecimal("10.00");
		return new StringBuilder().append(input)
			.append(number.toString())
			.toString();
	}

	public String testConcatWithStreamResult(String input) {
		List<String> values = Arrays.asList("val1", "val2", input);
		return new StringBuilder().append(input)
			.append(values.stream()
				.filter(s -> s.equals(input))
				.collect(Collectors.joining(",")))
			.append(values.stream()
				.collect(Collectors.joining(";")))
			.toString();
	}

	public String testConcatEmptyString(String input) {
		return new StringBuilder().append(input)
			.append("")
			.toString();
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
		List<String> values = Arrays.asList("val1", "val2", input);

		if ("".equals(result)) {
			result = new StringBuilder().append(result)
				.append(input)
				.toString();
		}

		switch (result) {
		case "some-val":
			result = new StringBuilder().append(result)
				.append(parameter)
				.toString();
			break;
		default:
			result = new StringBuilder().append(result)
				.append(parameter)
				.toString();
		}
		result = new StringBuilder().append(result)
			.append(";")
			.toString();

		for (String key : values) {
			result = new StringBuilder().append(result)
				.append(key)
				.append(",")
				.toString();
		}

		result = new StringBuilder().append(result)
			.append(";")
			.toString();

		return result;
	}

	public String testConcatWithStaticField(String input) {
		return new StringBuilder().append(input)
			.append(STATIC_VALUE)
			.toString();
	}

	public String testDiscardConcatResult(String input, String parameter) {
		input.concat(parameter);
		return new StringBuilder().append(input)
			.append(parameter)
			.toString();
	}

	public String testConcatInMethodInvocationParam(String input, String param) {
		boolean startsWitParam = input.startsWith((new StringBuilder().append(param)
			.append("a")
			.toString()), 0);
		return new StringBuilder().append(input)
			.append(Boolean.toString(startsWitParam))
			.toString();
	}

	public String testConcatRecursionWithLiteral_saveComments(String input) {
		// save comment 1
		return new StringBuilder().append(input)
			.append("abc")
			.append("def")
			.toString();
	}
}
