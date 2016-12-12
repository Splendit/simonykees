package at.splendit.simonykees.sample.postRule.allRules;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("nls")
public class TestForToForEachRule {

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	private List<Integer> generateHashCodeList(String input) {
		List<String> foo = generateList(input);
		List<Integer> fooHashCodes = foo.stream().map(s -> s.hashCode()).collect(Collectors.toList());
		return fooHashCodes;
	}

	public String testConvertIteratorToForEach(String input) {
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

	public String testIteratingIndex(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		int i;
		for (java.lang.String fooIterator : foo) {
			String s = fooIterator;
			sb.append(s);
		}

		return sb.toString();
	}

	public String testBiggerThanOneIterationStep(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		int i;
		for (i = 0; i < foo.size(); i += 2) {
			String s = foo.get(i);
			sb.append(s);
		}

		return sb.toString();
	}

	public String testMultipleInitStatements(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		int i, a;
		for (i = 0, a = 0; i < foo.size(); i++) {
			String s = foo.get(i);
			sb.append(s);
		}

		return sb.toString();
	}

	public String testMultipleIncStatements(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		int i, a;
		for (i = 0, a = 0; i < foo.size(); i++, a++) {
			String s = foo.get(i);
			sb.append(s);
		}

		return sb.toString();
	}

	public String testForToForEachWithArray(String input) {

		List<String> foo = generateList(input);
		// FIXME: SIM-159: forEach on the arrays is to be implemented
		String[] ms = (String[]) foo.toArray();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < ms.length; i++) {
			String s = ms[i];
			sb.append(s);
		}

		return sb.toString();
	}

	// public String testIterateNumberCollection(String input) {
	// List<? extends Number> foo = generateHashCodeList(input);
	//
	// StringBuilder sb = new StringBuilder();
	//
	// int i;
	// for (i = 0; i < foo.size(); i = i + 1) {
	// FIXME SIM-163 : if the collection type restricted to certain sub-types,
	// the forEach iterator type shall be 'parent type'
	// Number s = foo.get(i);
	// sb.append(s.toString());
	// }
	//
	// return sb.toString();
	// }

	public String testCollectionBiggerIterationStep(String input) {
		List<? extends Number> foo = generateHashCodeList(input);

		StringBuilder sb = new StringBuilder();

		int i;
		for (i = 0; i < foo.size(); i += 2) {
			Number s = foo.get(i);
			sb.append(s.toString());
		}

		return sb.toString();
	}
}
