package at.splendit.simonykees.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({ "nls", "unused", "unchecked" })
public class TestForToForEachRule {

	private List<String> a;

	public String testConvertIteratorToForEach(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			sb.append(s);
		}
		return sb.toString();
	}

	public String testReferencingIterator(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext();) {
			iterator.forEachRemaining(remaining -> remaining = "foo");
			String s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testIteratorToForEachIncrementStatement(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		int i = 0;
		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext(); i++) {
			String s = iterator.next();
			sb.append(s + "," + i);
		}
		sb.append(i);

		return sb.toString();
	}

	public String testIteratorToForEachNestedIf(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext();) {
			String s = iterator.next();
			if (iterator.hasNext()) {
				String t = iterator.next();
				sb.append(t);
			}
			sb.append(s + ",");
		}

		return sb.toString();
	}

	public String testNestedIteratorToForEach(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : foo) {
			for (String t : foo) {
				sb.append(t + ",");
			}
			sb.append(s + ";");
		}

		return sb.toString();
	}

	public String testMultipleIteratorToForEach(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (Iterator<String> iterator = foo.iterator(), it2 = foo.iterator(); iterator.hasNext();) {
			String anotherString = "foo";
			String s = iterator.next();
			String t = it2.next();
			sb.append(s + t);
		}
		return sb.toString();
	}

	public String testIteratorDiscardValue(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String fooIterator : foo) {
			String anotherString = "foo";
			sb.append(anotherString);
		}
		return sb.toString();
	}

	public void testForToForEach2(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < foo.size(); i++) {
			String s = foo.get(i);
			sb.append(s);
			sb.append(foo.get(i));
		}
	}

	public String testIteratingIndex(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		int i;
		for (i = 0; i < foo.size(); i++) {
			String s = foo.get(i);
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

		for (int j = 0; j < foo.size(); j++) {
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

		for (int i = 0; i < foo.size(); i++) {
			String s = foo.get(i);
			s += ";";
			sb.append(s);
			for (int j = 0; j < secondFoo.size(); j++) {
				String r = secondFoo.get(j);
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

		for (int i = 0; i < foo.size(); i++) {
			String s = foo.get(i);
			s += ";";
			sb.append(s);
			for (int j = 0; j < foo.size(); j++) {
				String r = foo.get(j);
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

		for (int i = 0; i < stFoo.size(); i++) {
			String s = stFoo.get(i);
			s += ";";
			sb.append(s);
			for (int j = 0; j < ndFoo.size(); j++) {
				String n = ndFoo.get(j);
				sb.append(n + ",");
				for (int k = 0; k < rdFoo.size(); k++) {
					String t = stFoo.get(i);
					String r = rdFoo.get(k);
					sb.append(r + t);
				}
			}
		}

		return sb.toString();
	}

	public String testCascadeForLoops(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();

		int i;
		for (i = 0; i < foo.size(); i++) {
			String it = foo.get(i);
			String someConstant = "const";
			sb.append(it + someConstant);
		}

		int j;
		for (j = 0; j < foo.size(); j++) {
			String it = foo.get(j);
			String someConstant = "const";
			sb.append(it + someConstant);
		}

		return sb.toString();
	}

	public String testIfNestedForLoops(String input) {
		List<String> foo = generateList(input);

		StringBuilder sb = new StringBuilder();
		if (foo != null) {
			for (int i = 0; i < foo.size(); i++) {
				String it = foo.get(i);
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
				for (int i = 0; i < foo.size(); i++) {
					String someConstant = "const";
					try {
						sb.append(foo.get(i) + someConstant);
					} finally {
						String s = foo.get(i);
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

	public String testIterateWithSizeNumberCollection(String input) {
		List<? extends Number> foo = generateHashCodeList(input);

		StringBuilder sb = new StringBuilder();

		int i;
		for (i = 0; i < foo.size(); i += 1) {
			// FIXME SIM-212
			Number s = foo.get(i);
			sb.append(s.toString());
		}

		return sb.toString();
	}

	public String testIterateNumberCollection(String input) {
		List<? extends Number> foo = generateHashCodeList(input);

		StringBuilder sb = new StringBuilder();

		for (Number s : foo) {
			sb.append(s.toString());
		}

		return sb.toString();
	}

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

	public String testIteratingIndexMoreLevels(String input) {
		a = generateList(input);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.size(); i++) {
			sb.append(this.a.get(i));

		}
		return sb.toString();
	}

	public Object encode(final Object value) {
		if (value != null) {
			Map<String, Object> map = new LinkedHashMap<>();
			List<Object> list = (List<Object>) value;
			for (int i = 0; i < list.size(); i++) {
				map.put(Integer.toString(i), list.get(i));
			}
			return map;
		}

		return null;
	}

	public boolean testDoubleLoop() {
		List<Double> coordinates = new ArrayList<>();
		Point point = new Point();

		for (int i = 0; i < coordinates.size(); i++) {
			final Double coordinate = coordinates.get(i);
			if (Double.compare(coordinate, point.getCoordinates().get(i)) != 0) {
				return false;
			}
		}
		return true;
	}

	public boolean testIteratingNonJavaIterators() {
		MyCollection<Number> myCollection = new MyCollection<>();

		for (Iterator<Number> iterator = myCollection.iterator(); iterator.hasNext();) {
			Number c = iterator.next();
			// do nothing
		}
		return false;
	}

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	private List<Integer> generateHashCodeList(String input) {
		List<String> foo = generateList(input);
		List<Integer> fooHashCodes = foo.stream().map(s -> s.hashCode()).collect(Collectors.toList());
		return fooHashCodes;
	}

	private class Point {
		private final List<Double> coordinates = new ArrayList<>();

		public List<Double> getCoordinates() {
			return coordinates;
		}
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

		public Iterator<T> iterator() {
			return new Iterator<T>() {

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public T next() {
					return null;
				}
			};
		}
	}

}
