package at.splendit.simonykees.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({ "nls", "unused", "rawtypes" })
public class TestForToForEachListIteratingIndexRule {

	String iterator;
	Runnable r = () -> {
		List<String> fInterfaceRule = generateList("");
		StringBuilder sb = new StringBuilder();
		for (String iterator1 : fInterfaceRule) {
			sb.append(iterator1);
		}
	};

	public String testRawType(String input) {
		List rawList = generateList(input);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rawList.size(); i++) {
			sb.append(rawList.get(i));
		}
		return sb.toString();
	}

	public String testWildCard(String input) {
		List<?> fooList = generateList(input);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fooList.size(); i++) {
			sb.append(fooList.get(i));
		}
		return sb.toString();
	}

	public String testIeratingThroughListOfLists(String input) {
		List<List<String>> nestedList = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (List<String> val : nestedList) {
			for (String iterator : val) {
				sb.append(iterator);
			}
		}
		return "";
	}

	public String testDublicateIteratorName(String input) {
		List<String> fooList = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (String iterator : fooList) {
			sb.append(iterator);
			for (String iterator1 : fooList) {
				sb.append(iterator + input + iterator1);
			}
		}
		return "";
	}

	public void testForToForEach2(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			sb.append(s);
			sb.append(s);
		}
	}

	public String testIteratingIndex(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			sb.append(s);
		}

		return sb.toString();
	}

	public String tesDuplicateIteratorName(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();
		int j;

		for (String iterator : foo) {
			// i want my comments here
			if (foo.size() > 0) {
				String s = iterator;
				"".equals(iterator);
				sb.append(s);
			} else {
				String s = iterator, d;
				"".equals(iterator);
				sb.append(s);
			}

		}

		return sb.toString();
	}

	public String tesLoopCondition(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();
		int i = 0, j;

		for (i = 0; i <= foo.size(); i++) {
			// i want my comments here
			String s = foo.get(i);
			"".equals(foo.get(i));
			sb.append(s);

		}

		return sb.toString();
	}

	public String testDecIteratingIndex(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		int i;
		for (i = foo.size() - 1; i >= 0; i--) {
			String s = foo.get(i);
			sb.append(s);
		}

		return sb.toString();
	}

	public String testModifiedIteratingIndex(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < foo.size(); i++) {
			String it = foo.get(i);
			String s = foo.get(i % 2);
			String firstString = foo.get(0);
			String someConstant = "const";
			sb.append(i + it + s + firstString + someConstant);
		}

		return sb.toString();
	}

	public String testIgnoreIteratingIndex(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String iterator : foo) {
			int i = 0;
			int k = 0;
			String it = foo.get(i);
			String it2 = foo.get(k);

			sb.append(it + "," + it2 + ";");
		}

		return sb.toString();
	}

	public String testCompoundCondition(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i + 1 < foo.size(); i++) {
			String it = foo.get(i);
			String s = foo.get(i % 2);
			String firstString = foo.get(0);
			String someConstant = "const";
			sb.append(i + it + s + firstString + someConstant);
		}

		return sb.toString();
	}

	public String testStartingIndex(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < foo.size(); i++) {
			String it = foo.get(i);
			String someConstant = "const";
			sb.append(i + it + someConstant);
		}

		return sb.toString();
	}

	public String testNestedForLoopsIteratingIndex(String input) {
		List<String> foo = generateList(input);
		List<String> secondFoo = generateList(input);

		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			s += ";";
			sb.append(s);
			for (String r : secondFoo) {
				sb.append(r);
			}
		}

		return sb.toString();
	}

	public String testDoubleIteration(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();
		String s, t;
		for (String fooIterator2 : foo) {
			s = fooIterator2;
			s += ";";
			sb.append(s);
			for (String fooIterator : foo) {
				t = fooIterator;
				sb.append(t);
			}
		}

		return sb.toString();
	}

	// SIM-212
	public String testDoubleIterationWithSize(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			s += ";";
			sb.append(s);
			for (String r : foo) {
				sb.append(r);
			}
		}

		return sb.toString();
	}

	public String testTripleNestedForLoops(String input) {
		List<String> stFoo = generateList(input);
		List<String> ndFoo = generateList(input);
		List<String> rdFoo = generateList(input);

		StringBuilder sb = new StringBuilder();

		for (String s : stFoo) {
			s += ";";
			sb.append(s);
			for (String n : ndFoo) {
				sb.append(n + ",");
				for (String r : rdFoo) {
					String t = s;
					sb.append(r + t);
				}
			}
		}

		return sb.toString();
	}

	public String testCascadeForLoops(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		for (String it : foo) {
			String someConstant = "const";
			sb.append(it + someConstant);
		}

		for (String it : foo) {
			String someConstant = "const";
			sb.append(it + someConstant);
		}

		return sb.toString();
	}

	public String testIfNestedForLoops(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();
		if (foo != null) {
			for (String it : foo) {
				String someConstant = "const";
				sb.append(it + someConstant);
			}
		}

		return sb.toString();
	}

	public String testTryCatchNestedForLoops(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();
		try {
			if (foo != null) {
				for (String s : foo) {
					String someConstant = "const";
					try {
						sb.append(s + someConstant);
					} finally {
						sb.append(",");
					}
				}
			}
		} catch (Exception e) {
			sb.append(e.getMessage());
		} finally {
			sb.append(";");
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

	public String testIterateWithSizeNumberCollection(String input) {
		List<? extends Number> foo = generateHashCodeList(input);

		StringBuilder sb = new StringBuilder();

		for (Number s : foo) {
			sb.append(s.toString());
		}

		return sb.toString();
	}

	public String testForToForEachNonIterable(String input) {

		MyCollection<String> foo = new MyCollection<>();
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < foo.size(); i++) {
			sb.append(foo.get(i));
		}

		return sb.toString();
	}

	public String testPlusEqualsUpdater(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			sb.append(s);
		}

		return sb.toString();
	}

	public String testPlusEqualsUpdaterInBody(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			sb.append(s);
		}

		return sb.toString();
	}

	public String testPrefixUpdater(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			sb.append(s);
		}

		return sb.toString();
	}

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	private List<Integer> generateHashCodeList(String input) {
		List<String> foo = generateList(input);
		List<Integer> fooHashCodes = foo.stream().map(s -> s.hashCode()).collect(Collectors.toList());
		return fooHashCodes;
	}

	@interface MyFooAnnotation {
		String iterator = "";
		Runnable r = () -> {
			String iterator;
			List<String> fInterfaceRule = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			for (String iterator1 : fInterfaceRule) {
				sb.append(iterator1);
			}
		};
	}

	/**
	 * This collection is not subtype of {@code Iterable}.
	 */
	private class MyCollection<T> {
		private final int size = 5;
		private int index = 0;

		public boolean hasNext() {
			return index < size;
		}

		public int size() {
			return 0;
		}

		public T get(int i) {
			return null;
		}
	}

}
