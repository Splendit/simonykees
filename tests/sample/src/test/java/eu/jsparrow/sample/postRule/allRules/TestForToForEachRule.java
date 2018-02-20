package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings({ "nls", "unused", "unchecked", "rawtypes" })
public class TestForToForEachRule {

	private List<String> a;

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	private List<Integer> generateHashCodeList(String input) {
		List<String> foo = generateList(input);
		List<Integer> fooHashCodes = foo.stream()
			.map(String::hashCode)
			.collect(Collectors.toList());
		return fooHashCodes;
	}

	public String unsafeIteratorName(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		String aL = "I am here to confuse you ~_^ ";

		// comment inside
		// comment after
		// comment before
		//
		l //
			.forEach(sb::append);
		return sb.toString();
	}

	public String emptyLoopCondition(String input) {
		StringBuilder sb = new StringBuilder();
		List<String> foo = generateList(input);
		for (Iterator<String> iterator = foo.iterator();;) {
			if (!iterator.hasNext()) {
				break;
			}
			sb.append(iterator.next());
		}
		return sb.toString();
	}

	public String testConvertIteratorToForEachTemp(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = foo.iterator();
		{
			for (; iterator.hasNext();) {
				// I have my comments
				String s = iterator.next();
				sb.append(s);
			}
		}
		return sb.toString();
	}

	public String testConvertIteratorToForEachTemp2(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		foo.forEach(sb::append);

		return sb.toString();
	}

	public String testConvertIteratorToForEach(String input) {
		List<String> foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		foo.forEach(sb::append);
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

		foo.forEach(s -> {
			foo.forEach(t -> sb.append(t + ","));
			sb.append(s + ";");
		});

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

		foo.forEach(aFoo -> {
			String anotherString = "foo";
			sb.append(anotherString);
		});
		return sb.toString();
	}

	public String testIterateNumberCollection(String input) {
		List<? extends Number> foo = generateHashCodeList(input);

		StringBuilder sb = new StringBuilder();

		foo.forEach(s -> sb.append(s.toString()));

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
			if (Double.compare(coordinate, point.getCoordinates()
				.get(i)) != 0) {
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

	public String rawIterable(String input) {
		List foo = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext();) {
			String s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
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
