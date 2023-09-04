package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "nls", "unused", "rawtypes", "unchecked" })
public class TestWhileToForEachRule {

	private static final Logger logger = LoggerFactory.getLogger(TestWhileToForEachRule.class);

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";")); //$NON-NLS-1$
	}

	public String unsafeIteratorName(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();
		final String aL = "I am here to confuse you!";
		//
		l //
			.forEach(sb::append);
		return sb.toString();
	}

	public String loopingOverArrays(String input) {
		final StringBuilder sb = new StringBuilder();
		final String[] array = { "-", input, "." };
		for (String t : array) {
			logger.info(t);
			sb.append(t);
		}
		return sb.toString();
	}

	public String loopingOverLists(String input) {
		final StringBuilder sb = new StringBuilder();
		final List<String> list = generateList(input);
		list.forEach(t -> {
			logger.info(t);
			sb.append(t);
		});
		return sb.toString();
	}

	public String testWhileToFor(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		l.forEach(sb::append);
		return sb.toString();
	}

	public String testWhileToFor2(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor3(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			s = iterator.next();
			s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor4(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			final String s = iterator.next();
			sb.append(s);
			iterator.remove();
			iterator.forEachRemaining(null);
		}
		return sb.toString();
	}

	public String testWhileToFor5(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		String s;
		for (String aL : l) {
			final Object k;
			s = aL;
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor6(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		String s;
		s = "lalelu";
		for (String aL : l) {
			final Object k;
			s = aL;
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor7(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		String s;
		for (String aL : l) {
			final Object k;
			s = aL;
			sb.append(s);
		}
		s = "lalelu";
		return sb.toString();
	}

	public String testWhileToFor8(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		String s = "";
		for (String aL : l) {
			final Object k;
			s = aL;
			sb.append(s);
		}
		sb.append(s);
		return sb.toString();
	}

	public String testNextOnlyIterator(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final Iterator<String> stringIterator = l.iterator();
		String s = null;
		while ((s = stringIterator.next()) != null) {
			sb.append(s);
		}
		return sb.toString();
	}

	public String testNestedWhileLoops(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		l.forEach(outerVal -> {
			sb.append(outerVal);

			l.forEach(sb::append);
		});

		return sb.toString();
	}

	public String testNestedWhileLoopsSingleBodyStatement(String input) {
		final List<String> l = generateList(input);
		final List<String> innerList = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final Iterator<String> innerIt = innerList.iterator();
		final Iterator<String> iterator = l.iterator();

		while (iterator.hasNext()) {
			while (innerIt.hasNext()) {
				sb.append(innerIt.next() + iterator.next());
			}
		}

		return sb.toString();
	}

	public String testNestedWhileLoopsSingleBodyStatement2(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final Iterator<String> innerIt = l.iterator();
		final Iterator<String> iterator = l.iterator();

		l.forEach(outerKey -> {
			while (innerIt.hasNext()) {
				sb.append(innerIt.next() + outerKey);
			}
		});

		return sb.toString();
	}

	public String testCascadedWhilesToFor(String input) {
		final List<String> l = generateList(input);
		final List<String> k = generateList(input);
		final StringBuilder sb = new StringBuilder();

		l.forEach(sb::append);

		k.forEach(sb::append);

		return sb.toString();
	}

	public String testTripleNestedWhilesToFor(String input) {
		final List<String> l = generateList(input);
		final List<String> k = generateList(input);
		final List<String> m = generateList(input);
		final StringBuilder sb = new StringBuilder();

		l.forEach(outerVal -> {
			sb.append(outerVal);

			// FIXME SIM-173: RuleException is thrown
			// Iterator<String> mIterator = m.iterator();
			// while (mIterator.hasNext()) {
			// String mVal = mIterator.next();
			// sb.append(mVal);
			// }
			k.forEach(sb::append);
		});

		return sb.toString();
	}

	public String testNestedIfWhilesToFor(String input) {
		final List<String> l = generateList(input);
		final List<String> k = generateList(input);
		final List<String> m = generateList(input);
		final StringBuilder sb = new StringBuilder();

		l.forEach(outerVal -> {
			sb.append(outerVal);

			final Iterator<String> kIterator = k.iterator();
			final String kVal;
			if ((kVal = kIterator.next()) != null) {
				sb.append(kVal);

				m.forEach(sb::append);
			}
		});

		return sb.toString();
	}

	public String testWhileLoopsMultipleDeclaration(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		l.forEach(m -> {
			final String n = "nothing";
			final Integer i = 1;
			final String o = "-";
			final String p = "something";
			sb.append(n);
			sb.append(m);
			sb.append(o);
			sb.append(p);
			sb.append(i.toString());
		});

		return sb.toString();
	}

	public String testWhileLoopsIgnoreIterator(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		l.forEach(aL -> sb.append("foo"));

		return sb.toString();
	}

	public String testWhileLoopsNoIteratingVariable(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final String s;
		final String foo = "foo";
		l.forEach(sb::append);

		return sb.toString();
	}

	public String testWhileLoopsRawList(String input) {
		final List l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final Iterator<String> iterator = l.iterator();
		final String s;
		final String foo = "foo";
		while (iterator.hasNext()) {
			sb.append(iterator.next());
		}

		return sb.toString();
	}

	public String testQualifiedNameIterator(String input) {
		final Foo foo = new Foo();
		foo.l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		foo.l.forEach(sb::append);

		return sb.toString();
	}

	public String testWhileLoopsCompoundCondition(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final Iterator<String> iterator = l.iterator();
		String s;
		final String foo = "foo";
		while (iterator.hasNext() && !StringUtils.isEmpty(foo)) {
			if (l.size() > 0) {
				s = iterator.next();
				sb.append(new StringBuilder().append(s)
					.append("|")
					.append(foo)
					.toString());
			}
		}

		return sb.toString();
	}

	public String testWhileLoopsWithSwitchCase(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();
		final String fooCase = "foo";

		for (String s : l) {
			switch (fooCase) {
			case "foo":
				sb.append(s);
				break;
			case "b":
				sb.append("b");
				break;
			case "c":
				sb.append("c");
				break;
			default:
				sb.append("nothing");
				break;
			}
		}

		return sb.toString();
	}

	public String testWhileLoopsWithNestedTryCatch(String input) {
		final List<String> l = generateList(input);
		final StringBuilder sb = new StringBuilder();

		String s;
		final String foo = "foo";
		String suffix = "";
		String prefix = "";
		for (String aL : l) {
			try {
				if (l.size() > 0) {
					s = aL;
					prefix = s;
				}
			} catch (Exception e) {
				s = e.getLocalizedMessage();
			} finally {
				suffix = "|" + foo;
			}

			sb.append(prefix + suffix);
		}

		return sb.toString();
	}

	public String testWhileLoopsWithNestedLambda(String input) {
		final List<String> l = generateList(input);
		final List<String> k = generateList(input);
		List<String> result = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();

		final String foo = "foo";
		final String suffix = "";
		final String prefix = "";
		for (String s : l) {
			result = k.stream()
				.map(key -> new StringBuilder().append(s)
					.append("|")
					.append(key)
					.append(";")
					.toString())
				.collect(Collectors.toList());

			result.forEach(sb::append);
		}
		return sb.toString();
	}

	public String testWhileLoopsNumericIterator(String input) {
		final List<String> l = generateList(input);
		final List<Number> numbers = l.stream()
			.map(String::hashCode)
			.collect(Collectors.toList());

		final StringBuilder sb = new StringBuilder();

		numbers.forEach(s -> {
			final String foo = "foo";
			sb.append(s);
		});
		return sb.toString();
	}

	// SIM-211
	public String testIteratorReuse(String input) {
		final List<String> l1 = generateList(input);
		final List<String> l2 = generateList(input);
		final StringBuilder sb = new StringBuilder();

		Iterator iterator = l1.iterator();
		while (iterator.hasNext()) {
			final String s = (String) iterator.next();
			final int i = StringUtils.length(s);
			sb.append(s)
				.append(i);
		}

		iterator = l2.iterator();

		while (iterator.hasNext()) {
			final String s = (String) iterator.next();
			final int i = StringUtils.length(s);
			sb.append(s)
				.append(i);
		}

		return sb.toString();
	}

	public String testNonIterableCollection(String input) {
		final StringBuilder sb = new StringBuilder();
		final FooCollection<Number> numbers = new FooCollection<>();
		final Iterator<Number> iterator = numbers.iterator();

		while (iterator.hasNext()) {
			final String foo = "foo";
			final Number s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * This collection is not subtype of {@code Iterable}.
	 */
	private class FooCollection<T> {
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

	class Foo {
		public List<String> l;
	}
}
