package eu.jsparrow.sample.postRule.allRules;

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
		/* between head and body */
		// comment inside
		// comment after
		// comment before
		/* init 2 */
		// comment after fInterfaceRule
		/* init 1 */
		/* inc */
		// comment after fInterfaceRule
		fInterfaceRule // comment after fInterfaceRule
			.forEach(sb::append);
	};

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	private List<Integer> generateHashCodeList(String input) {
		final List<String> foo = generateList(input);
		final List<Integer> fooHashCodes = foo.stream()
			.map(String::hashCode)
			.collect(Collectors.toList());
		return fooHashCodes;
	}

	public String testRawType(String input) {
		final List rawList = generateList(input);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rawList.size(); i++) {
			sb.append(rawList.get(i));
		}
		return sb.toString();
	}

	public String testWildCard(String input) {
		final List<?> fooList = generateList(input);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fooList.size(); i++) {
			sb.append(fooList.get(i));
		}
		return sb.toString();
	}

	public String testIeratingThroughListOfLists(String input) {
		final List<List<String>> nestedList = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		nestedList.stream()
			.flatMap(List::stream)
			.forEach(sb::append);
		return "";
	}

	public String testDublicateIteratorName(String input) {
		final List<String> fooList = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		fooList.forEach(aFooList -> {
			sb.append(aFooList);
			fooList.forEach(aFooList1 -> sb.append(new StringBuilder().append(aFooList)
				.append(input)
				.append(aFooList1)
				.toString()));
		});
		return "";
	}

	public void testForToForEach2(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		foo.forEach(s -> {
			sb.append(s);
			sb.append(s);
		});
	}

	public String testIteratingIndex(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		foo.forEach(sb::append);

		return sb.toString();
	}

	public String tesDuplicateIteratorName(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();
		final int j;

		foo.forEach(aFoo -> {
			// i want my comments here
			if (foo.size() > 0) {
				final String s = aFoo;
				"".equals(aFoo);
				sb.append(s);
			} else {
				final String s = aFoo;
				final String d;
				"".equals(aFoo);
				sb.append(s);
			}

		});

		return sb.toString();
	}

	public String tesLoopCondition(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();
		int i = 0;
		final int j;

		for (i = 0; i <= foo.size(); i++) {
			// i want my comments here
			final String s = foo.get(i);
			"".equals(foo.get(i));
			sb.append(s);

		}

		return sb.toString();
	}

	public String testDecIteratingIndex(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		int i;
		for (i = foo.size() - 1; i >= 0; i--) {
			final String s = foo.get(i);
			sb.append(s);
		}

		return sb.toString();
	}

	public String testModifiedIteratingIndex(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < foo.size(); i++) {
			final String it = foo.get(i);
			final String s = foo.get(i % 2);
			final String firstString = foo.get(0);
			final String someConstant = "const";
			sb.append(new StringBuilder().append(i)
				.append(it)
				.append(s)
				.append(firstString)
				.append(someConstant)
				.toString());
		}

		return sb.toString();
	}

	public String testIgnoreIteratingIndex(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		for (int j = 0; j < foo.size(); j++) {
			final int i = 0;
			final int k = 0;
			final String it = foo.get(i);
			final String it2 = foo.get(k);

			sb.append(new StringBuilder().append(it)
				.append(",")
				.append(it2)
				.append(";")
				.toString());
		}

		return sb.toString();
	}

	public String testCompoundCondition(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i + 1 < foo.size(); i++) {
			final String it = foo.get(i);
			final String s = foo.get(i % 2);
			final String firstString = foo.get(0);
			final String someConstant = "const";
			sb.append(new StringBuilder().append(i)
				.append(it)
				.append(s)
				.append(firstString)
				.append(someConstant)
				.toString());
		}

		return sb.toString();
	}

	public String testStartingIndex(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		for (int i = 1; i < foo.size(); i++) {
			final String it = foo.get(i);
			final String someConstant = "const";
			sb.append(new StringBuilder().append(i)
				.append(it)
				.append(someConstant)
				.toString());
		}

		return sb.toString();
	}

	public String testNestedForLoopsIteratingIndex(String input) {
		final List<String> foo = generateList(input);
		final List<String> secondFoo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		foo.forEach(s -> {
			s += ";";
			sb.append(s);
			secondFoo.forEach(sb::append);
		});

		return sb.toString();
	}

	public String testDoubleIteration(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();
		String s;
		String t;
		for (String aFoo2 : foo) {
			s = aFoo2;
			s += ";";
			sb.append(s);
			for (String aFoo : foo) {
				t = aFoo;
				sb.append(t);
			}
		}

		return sb.toString();
	}

	// SIM-212
	public String testDoubleIterationWithSize(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		foo.forEach(s -> {
			s += ";";
			sb.append(s);
			foo.forEach(sb::append);
		});

		return sb.toString();
	}

	public String testTripleNestedForLoops(String input) {
		final List<String> stFoo = generateList(input);
		final List<String> ndFoo = generateList(input);
		final List<String> rdFoo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		for (String s : stFoo) {
			s += ";";
			sb.append(s);
			for (String n : ndFoo) {
				sb.append(n + ",");
				for (String r : rdFoo) {
					final String t = s;
					sb.append(r + t);
				}
			}
		}

		return sb.toString();
	}

	public String testCascadeForLoops(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		foo.forEach(it -> {
			final String someConstant = "const";
			sb.append(it + someConstant);
		});

		foo.forEach(it -> {
			final String someConstant = "const";
			sb.append(it + someConstant);
		});

		return sb.toString();
	}

	public String testIfNestedForLoops(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();
		if (foo != null) {
			foo.forEach(it -> {
				final String someConstant = "const";
				sb.append(it + someConstant);
			});
		}

		return sb.toString();
	}

	public String testTryCatchNestedForLoops(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();
		try {
			if (foo != null) {
				foo.forEach(s -> {
					final String someConstant = "const";
					try {
						sb.append(s + someConstant);
					} finally {
						sb.append(",");
					}
				});
			}
		} catch (Exception e) {
			sb.append(e.getMessage());
		} finally {
			sb.append(";");
		}

		return sb.toString();
	}

	public String testBiggerThanOneIterationStep(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		int i;
		for (i = 0; i < foo.size(); i += 2) {
			final String s = foo.get(i);
			sb.append(s);
		}

		return sb.toString();
	}

	public String testMultipleInitStatements(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		int i;
		final int a;
		for (i = 0, a = 0; i < foo.size(); i++) {
			final String s = foo.get(i);
			sb.append(s);
		}

		return sb.toString();
	}

	public String testMultipleIncStatements(String input) {
		final List<String> foo = generateList(input);

		final StringBuilder sb = new StringBuilder();

		int i;
		int a;
		for (i = 0, a = 0; i < foo.size(); i++, a++) {
			final String s = foo.get(i);
			sb.append(s);
		}

		return sb.toString();
	}

	public String testIterateWithSizeNumberCollection(String input) {
		final List<? extends Number> foo = generateHashCodeList(input);

		final StringBuilder sb = new StringBuilder();

		// FIXME SIM-212
		foo.forEach(s -> sb.append(s.toString()));

		return sb.toString();
	}

	public String testForToForEachNonIterable(String input) {

		final MyCollection<String> foo = new MyCollection<>();
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < foo.size(); i++) {
			sb.append(foo.get(i));
		}

		return sb.toString();
	}

	public String testPlusEqualsUpdater(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		foo.forEach(sb::append);

		return sb.toString();
	}

	public String testPlusEqualsUpdaterInBody(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		foo.forEach(sb::append);

		return sb.toString();
	}

	public String testPrefixUpdater(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		foo.forEach(sb::append);

		return sb.toString();
	}

	public void stringTemplate4CornerCase() {

		final List<Object> exprs = new ArrayList<>();
		for (int i = 0; i < exprs.size(); i++) {
			final Object attr = exprs.get(i);
			if (attr != null) {
				exprs.set(i, null);

			}
		}
	}

	public String avoidEmptyStatement(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < foo.size(); i++) {
			foo.get(i);
			sb.append(foo.get(i));
		}

		return sb.toString();
	}

	public static boolean hasDuplicateItems(List<Object> array) {
		// jFreeChart corner case
		for (int i = 0; i < array.size(); i++) {
			for (int j = 0; j < i; j++) {
				final Object o1 = array.get(i);
				final Object o2 = array.get(j);
				final boolean condition = o1 != null && o2 != null && o1.equals(o2);
				if (condition) {
					return true;
				}
			}
		}
		return false;
	}

	public String rawIteratingObject(String input) {
		final List<List<String>> listOfLists = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < listOfLists.size(); i++) {
			final List rawIterator = listOfLists.get(i);
			// Incorrect casting to double
			final Double d = (Double) rawIterator.get(0);
			sb.append(d);
		}

		return sb.toString();
	}

	public <T extends Foo> void listOfTypeArguments() {
		final List<T> elements = new ArrayList<>();
		elements.forEach(foo -> {
			foo.toString();
			foo.isFoo();
		});
	}

	public <T extends Foo> void captureOfTypeArguments() {
		final List<? extends T> elements = new ArrayList<>();
		elements.forEach(foo -> {
			foo.toString();
			foo.isFoo();
		});
	}

	public <T extends MyCollection<String>> void listOfParameterizedTypeArguments() {
		final List<T> elements = new ArrayList<>();
		elements.forEach(foo -> {
			foo.toString();
			foo.hasNext();
		});
	}

	public String qualifiedNameType() {
		final List<java.lang.Boolean> javaLangBooleans = Arrays.asList(true, true, false);
		final StringBuilder sb = new StringBuilder();
		javaLangBooleans.forEach(sb::append);
		return sb.toString();
	}

	public String testSName(String input) {
		final List<String> s = generateList(input);
		final StringBuilder sb = new StringBuilder();
		s.forEach(sb::append);
		return sb.toString();
	}

	public String packageKeyWord(String input) {
		final List<String> packages = generateList(input);
		final StringBuilder sb = new StringBuilder();
		packages.forEach(sb::append);
		return sb.toString();
	}

	public String doubleKeyWord(String input) {
		final List<Double> doubles = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		doubles.forEach(sb::append);
		return sb.toString();
	}

	public static Foo createFoo(String input) {
		return new TestForToForEachListIteratingIndexRule().new Foo(input);
	}

	@interface MyFooAnnotation {
		String iterator = "";
		Runnable r = () -> {
			String iterator;
			List<String> fInterfaceRule = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			// multiple
			// line
			// comment
			fInterfaceRule.forEach(sb::append);
		};
	}

	private class GenericClassSample<T> {
		class InnerType {

			public void useInnerCollection(List<InnerType> myInnerCList) {
				int size = 0;
				for (GenericClassSample<T>.InnerType innerCObje : myInnerCList) {
					size++;
				}
			}
		}
	}

	class Foo {
		private final String foo;

		public Foo(String foo) {
			this.foo = foo;
		}

		@Override
		public String toString() {
			return this.foo;
		}

		public boolean isFoo() {
			return true;
		}
	}

	/**
	 * This collection is not subtype of {@code Iterable}.
	 */
	private class MyCollection<T> {
		private final int size = 5;
		private final int index = 0;

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

	private class Boolean {
		public boolean val = false;
	}
}
