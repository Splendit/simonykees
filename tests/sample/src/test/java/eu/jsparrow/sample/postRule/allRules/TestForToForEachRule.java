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
		final List<String> foo = generateList(input);
		final List<Integer> fooHashCodes = foo.stream()
			.map(String::hashCode)
			.collect(Collectors.toList());
		return fooHashCodes;
	}

	public String unsafeIteratorName(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();
		final String aL = "I am here to confuse you ~_^ ";

		// comment inside
		// comment after
		// comment before
		//
		l //
			.forEach(sb::append);
		return sb.toString();
	}

	public String emptyLoopCondition(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> foo = generateList(input);
		for (Iterator<String> iterator = foo.iterator();;) {
			if (!iterator.hasNext()) {
				break;
			}
			sb.append(iterator.next());
		}
		return sb.toString();
	}

	public String testConvertIteratorToForEachTemp(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final Iterator<String> iterator = foo.iterator();
		{
			for (; iterator.hasNext();) {
				// I have my comments
				final String s = iterator.next();
				sb.append(s);
			}
		}
		return sb.toString();
	}

	public String testConvertIteratorToForEachTemp2(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		foo.forEach(sb::append);

		return sb.toString();
	}

	public String testConvertIteratorToForEach(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		foo.forEach(sb::append);
		return sb.toString();
	}

	public String testReferencingIterator(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext();) {
			iterator.forEachRemaining(remaining -> remaining = "foo");
			final String s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testIteratorToForEachIncrementStatement(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		int i = 0;
		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext(); i++) {
			final String s = iterator.next();
			sb.append(new StringBuilder().append(s)
				.append(",")
				.append(i)
				.toString());
		}
		sb.append(i);

		return sb.toString();
	}

	public String testIteratorToForEachNestedIf(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext();) {
			final String s = iterator.next();
			if (iterator.hasNext()) {
				final String t = iterator.next();
				sb.append(t);
			}
			sb.append(s + ",");
		}

		return sb.toString();
	}

	public String testNestedIteratorToForEach(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		foo.forEach(s -> {
			foo.forEach(t -> sb.append(t + ","));
			sb.append(s + ";");
		});

		return sb.toString();
	}

	public String testMultipleIteratorToForEach(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		for (Iterator<String> iterator = foo.iterator(), it2 = foo.iterator(); iterator.hasNext();) {
			final String anotherString = "foo";
			final String s = iterator.next();
			final String t = it2.next();
			sb.append(s + t);
		}
		return sb.toString();
	}

	public String testIteratorDiscardValue(String input) {
		final List<String> foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		foo.forEach(aFoo -> {
			final String anotherString = "foo";
			sb.append(anotherString);
		});
		return sb.toString();
	}

	public String testIterateNumberCollection(String input) {
		final List<? extends Number> foo = generateHashCodeList(input);

		final StringBuilder sb = new StringBuilder();

		foo.forEach((Number s) -> sb.append(s.toString()));

		return sb.toString();
	}

	public String testCollectionBiggerIterationStep(String input) {
		final List<? extends Number> foo = generateHashCodeList(input);

		final StringBuilder sb = new StringBuilder();

		int i;
		for (i = 0; i < foo.size(); i += 2) {
			final Number s = foo.get(i);
			sb.append(s.toString());
		}

		return sb.toString();
	}

	public String testIteratingIndexMoreLevels(String input) {
		a = generateList(input);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.size(); i++) {
			sb.append(this.a.get(i));

		}
		return sb.toString();
	}

	public Object encode(final Object value) {
		if (value == null) {
			return null;
		}
		final Map<String, Object> map = new LinkedHashMap<>();
		final List<Object> list = (List<Object>) value;
		for (int i = 0; i < list.size(); i++) {
			map.put(Integer.toString(i), list.get(i));
		}
		return map;
	}

	public boolean testDoubleLoop() {
		final List<Double> coordinates = new ArrayList<>();
		final Point point = new Point();

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
		final MyCollection<Number> myCollection = new MyCollection<>();

		for (Iterator<Number> iterator = myCollection.iterator(); iterator.hasNext();) {
			final Number c = iterator.next();
			// do nothing
		}
		return false;
	}

	public String rawIterable(String input) {
		final List foo = generateList(input);
		final StringBuilder sb = new StringBuilder();

		for (Iterator<String> iterator = foo.iterator(); iterator.hasNext();) {
			final String s = iterator.next();
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
		private final int index = 0;

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
