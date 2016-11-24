package at.splendit.simonykees.sample.postRule.allRules;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("nls")
public class TestForToForEachRule {

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	public String testForToForEach(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			sb.append(s);
		}
		return sb.toString();
	}

	public void testForToForEach2(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (java.lang.String fooIterator : foo) {
			String s = fooIterator;
			sb.append(s);
			sb.append(fooIterator);
		}
	}
}
