package at.splendit.simonykees.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings({ "nls", "unused" })
public class LambdaForEachMapRule {

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	public String unwrapOneExpression(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> StringUtils.substring(s, 1)).forEach(sb::append);

		return sb.toString();
	}

	public String unwrapMultipleExpressions(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> {
			int i = 10;
			return StringUtils.substring(s, 1) + i;
		}).forEach(subString -> {
			String lower = StringUtils.lowerCase(subString);
			sb.append(lower);
		});

		return sb.toString();
	}

	public String unsplittableBody(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		list.stream().filter(s -> !StringUtils.isEmpty(s)).forEach(s -> {
			int i = 10;
			String subString = StringUtils.substring(s, 1) + i;
			String lower = StringUtils.lowerCase(subString);
			sb.append(lower + i);
		});
		return sb.toString();
	}

	public String unsplittableBody2(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		list.stream().filter(s -> !StringUtils.isEmpty(s)).forEach(s -> {
			int i = 10;
			int c = 0;
			String subString = StringUtils.substring(s, 1) + i + c;
			String lower = StringUtils.lowerCase(subString);
			sb.append(lower + c);
		});
		return sb.toString();
	}

	public String unsplittableBody3(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		list.stream().filter(s -> !StringUtils.isEmpty(s)).forEach(s -> {
			int i = 10;
			int c = 0;
			String subString = StringUtils.substring(s, 1) + i + c;
			String lower = StringUtils.lowerCase(subString);
			sb.append(lower);
			if (StringUtils.isEmpty(lower)) {
				sb.append(s);
			}
		});
		return sb.toString();
	}

	public String mapToDifferentType(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> {
			int offset = 10;
			return StringUtils.indexOf(s, "i") + offset;
		}).forEach(pos -> sb.append(Integer.toString(pos)));
		return sb.toString();
	}

	public String mapToDifferentTypeSingleBodyExpression(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> StringUtils.indexOf(s, "i") + 10)
				.forEach(pos -> sb.append(Integer.toString(pos)));
		return sb.toString();
	}

	public String ifStatementAfterMappingVar(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		int offset = 10;
		list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> {
			sb.append(s);
			sb.append("c");
			return StringUtils.indexOf(s, "i") + 10;
		}).forEach(pos -> {
			sb.append("d");
			if (offset > 0) {
				sb.append(Integer.toString(pos));
			}
		});
		return sb.toString();
	}

	public String ifStatementBeforeMappingVar(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		int offset = 10;
		list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> {
			if (offset > 0) {
				sb.append(s);
			}
			return StringUtils.indexOf(s, "i");
		}).forEach(pos -> {
			sb.append("c");
			sb.append(pos + "d");
		});
		return sb.toString();
	}

	public String saveComments(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		int offset = 10;
		list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> {
			// I may be necessary here
			if (offset > 0) {
				sb.append(s);
			}
			return StringUtils.indexOf(s, "i");
		}).forEach(pos -> {
			// and here...
			sb.append("c");
			sb.append(pos + "d");
		});
		return sb.toString();
	}

	public String saveComments2(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		int offset = 10;
		list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> {
			int i;// not important
			// not used
			int j;
			return StringUtils.indexOf(s, "i");
		}).forEach(pos -> {
			sb.append("c");
			sb.append(pos + "d");
		});
		return sb.toString();
	}

	public String multipleDeclarationFragments(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();

		list.stream().filter(s -> !StringUtils.isEmpty(s)).map(s -> {
			sb.append(s);
			return StringUtils.indexOf(s, "i");
		}).forEach(pos -> {
			int c = 0;
			sb.append("c");
			sb.append(pos + "d");
		});
		return sb.toString();
	}

	public String explicitParameterType() {
		List<Number> numbers = new ArrayList<>();
		numbers.add(2.3);
		numbers.add(4.5);

		StringBuilder sb = new StringBuilder();
		numbers.stream().filter(n -> n.doubleValue() > 0).map((Number n) -> (Double) n).forEach((Double d) -> {
			String s = d.toString();
			sb.append(s);
		});

		return sb.toString();
	}
}
