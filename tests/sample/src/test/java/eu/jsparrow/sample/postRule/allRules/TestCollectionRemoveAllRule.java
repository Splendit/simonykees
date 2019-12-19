package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({ "nls", "unused" })
public class TestCollectionRemoveAllRule {
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	public String testIfCollectionIsEmpty(String input) {
		final List<String> resultList = generateList(input);

		/* invocation comment */
		/* param comment */
		resultList /* expression comment */.clear();

		resultList // I don't want to break anything
			.clear();

		final StringBuilder sb = new StringBuilder();

		resultList.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testProperCollectionIsEmpty(String input) {
		final List<String> resultList = generateList(input);
		final List<String> resultList2 = generateList(input);
		resultList2.add("d");

		resultList.removeAll(resultList2);

		final StringBuilder sb = new StringBuilder();

		resultList2.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testConvertMultipleCollections(String input) {
		final List<String> resultList1 = generateList(input);
		final List<String> resultList2 = generateList(input);
		final List<String> resultList3 = generateList(input);

		resultList2.add("d");

		resultList1.clear();
		resultList2.clear();
		resultList3.clear();

		final StringBuilder sb = new StringBuilder();

		resultList1.stream()
			.forEach(sb::append);
		resultList2.stream()
			.forEach(sb::append);
		resultList3.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testNestedIf(String input) {
		final List<String> resultList = generateList(input);
		final List<String> resultList2 = generateList(input);
		resultList2.add("d");

		if (resultList2.isEmpty()) {
			resultList.clear();
		}

		final StringBuilder sb = new StringBuilder();

		resultList2.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testNestedFor(String input) {
		final List<String> resultList = generateList(input);
		final List<String> resultList2 = generateList(input);
		resultList2.add("d");

		resultList2.forEach(s -> {
			if (!resultList.isEmpty()) {
				resultList.clear();
			}
		});

		final StringBuilder sb = new StringBuilder();

		resultList2.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testNestedLambda(String input) {
		final List<String> resultList = generateList(input);
		final List<String> resultList2 = generateList(input);
		resultList2.add("d");

		resultList2.forEach(s -> {
			if (!resultList.isEmpty()) {
				resultList.clear();
			}
		});

		final StringBuilder sb = new StringBuilder();

		resultList2.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testConvertInSwitchCase(String input) {
		final List<String> resultList = generateList(input);
		final List<String> resultList2 = generateList(input);
		resultList2.add("d");

		switch (resultList2.size()) {
		case 0:
			resultList.clear();
			break;
		case 1:
			resultList.clear();
			break;
		default:
			resultList.clear();
		}

		final StringBuilder sb = new StringBuilder();

		resultList2.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testConvertInWhileLoop(String input) {
		final List<String> resultList = generateList(input);
		final List<String> resultList2 = generateList(input);
		resultList2.add("d");

		for (String s : resultList2) {
			switch (s) {
			case "a":
				resultList.clear();
				break;
			case "d":
				resultList.clear();
				break;
			}
		}

		final StringBuilder sb = new StringBuilder();
		resultList.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testEmptyCollection(String input) {
		final List<String> resultList = generateList(input);

		resultList.removeAll(new ArrayList<String>());

		final StringBuilder sb = new StringBuilder();

		resultList.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testModifiedCollection(String input) {
		final List<String> resultList = generateList(input);

		resultList.removeAll(resultList.stream()
			.collect(Collectors.toList()));

		final StringBuilder sb = new StringBuilder();

		resultList.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testReferencedCollection(String input) {
		final List<String> resultList = generateList(input);
		final List<String> resultList2 = resultList;

		resultList.removeAll(resultList2);

		final StringBuilder sb = new StringBuilder();

		resultList.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testNumericCollection(String input) {
		final List<String> resultList = generateList(input);
		final List<Number> numericList = resultList.stream()
			.map(String::hashCode)
			.collect(Collectors.toList());

		resultList.clear();
		numericList.clear();

		final StringBuilder sb = new StringBuilder();

		numericList.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testMultipleConvertionPerLine(String input) {
		final List<String> resultList = generateList(input);
		final List<Number> numericList = resultList.stream()
			.map(String::hashCode)
			.collect(Collectors.toList());

		resultList.clear();
		numericList.clear();
		numericList.clear();
		numericList.removeAll(resultList);

		final StringBuilder sb = new StringBuilder();

		numericList.stream()
			.forEach(sb::append);
		resultList.stream()
			.forEach(sb::append);

		return sb.toString();
	}

	public String testSavingComments(String input) {
		final List<String> resultList = generateList(input);

		// to not be lost
		resultList.clear();

		/* trailing comments */
		resultList.clear();

		// to not be lost
		resultList.clear();

		final StringBuilder sb = new StringBuilder();

		resultList.stream()
			.forEach(sb::append);

		return sb.toString();
	}
}
