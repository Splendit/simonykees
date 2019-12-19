package eu.jsparrow.sample.postRule.allRules;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.sample.utilities.Person;

/**
 * 
 * @author Matthias Webhofer
 * @since 1.2
 */
@SuppressWarnings({ "nls", "unused" })
public class EnhancedForLoopToStreamForEachRule {
	private static final Logger logger = LoggerFactory.getLogger(EnhancedForLoopToStreamForEachRule.class);
	public static List<String> stringList1 = Arrays.asList("str1", "str2", "str3", "str4");
	public static List<String> stringList2 = Arrays.asList("str1", "str2", "str3", "str4");
	public static List<String> stringList3;
	static {
		stringList3 = new LinkedList<>();
		stringList1.forEach(s -> stringList2.forEach(t -> stringList3.add(s + t)));
	}

	public List<List<String>> stringListList = Arrays.asList(stringList1, stringList2);
	private final TestClass testClassField = new TestClass();
	private int intField = 0;
	protected Map<String, Map<String, String>> validationConfigurations = new HashMap<>();

	public String doSomething() throws ClassNotFoundException, FileNotFoundException {

		// internal comment
		// comment after
		// save me
		/* comment on expression */
		stringList1 /* comment on expression */.forEach(logger::info);

		stringList1.forEach(logger::info);

		for (String s : stringList1) {
			if (s.length() > 5) {
				continue;
			} else if (s.length() > 3) {
				break;
			} else {
				return s;
			}
		}

		for (String s : stringList1) {
			if (s.length() > 5) {
				continue;
			}
			logger.info(s);
		}

		for (String s : stringList1) {
			if (s.length() > 5) {
				break;
			}
			logger.info(s);
		}

		for (String s : stringList1) {
			if (s.length() > 5) {
				return s;
			}
			logger.info(s);
		}

		stringList1.forEach(s -> stringList2.forEach(t -> logger.info(s + t)));

		for (List<String> list : stringListList) {
			for (String s : list) {
				for (String t : stringList2) {
					if (t.equals(s)) {
						break;
					}
					if (t.length() > s.length()) {
						continue;
					}
				}
			}
		}

		stringListList.forEach(list -> {
			stringList1.add(list.get(0));
			list.forEach(s -> stringList2.forEach(t -> {
				if (t.equals(s)) {
					logger.info(t);
				}
				if (t.length() > s.length()) {
					logger.info(s + t);
				}
			}));
		});

		for (String s : stringList1) {
			Class.forName(s);
		}

		stringList1.forEach(s -> {
			try {
				Class.forName(s);
			} catch (ClassNotFoundException cnfe) {
				logger.error(cnfe.getMessage(), cnfe);
				logger.info(s);
			}
		});

		stringList1.forEach(s -> {
			int length = 0;
			if (s.length() < 2) {
				length /= s.length();
			}
		});

		int length = 0;
		for (String s : stringList1) {
			length += s.length();
		}

		final int length2 = 0;
		stringList1.forEach(s -> {
			if (length2 > 0) {
				logger.info(String.valueOf(length2));
			}
		});

		int length3 = 0;
		for (String s : stringList1) {
			length3++;
			--length3;
			this.intField++;
			--this.intField;
		}

		int length4 = 0;
		for (String s : stringList1) {
			length4++;
		}

		int length5 = 0;
		for (String s : stringList1) {
			--length5;
		}

		String u = "asdf";
		for (String s : stringList1) {
			if (s.equals(u)) {
				logger.info(String.valueOf(u.length()));
			}
		}

		for (String s : stringList1) {
			if (s.length() > u.length()) {
				u = s;
			}
		}

		stringList1.forEach(s -> this.intField++);

		stringList1.forEach(s -> intField++);

		stringList1.forEach(s -> testClassField.testIntField++);

		stringList1.forEach(s -> --testClassField.testIntField);

		stringList1.forEach(s -> testClassField.testIntField += s.length());

		final TestClass testClassLocal = new TestClass();
		EnhancedForLoopToStreamForEachRule rule = new EnhancedForLoopToStreamForEachRule();

		stringList1.forEach(s -> testClassLocal.testIntField++);

		stringList1.forEach(s -> --testClassLocal.testIntField);

		stringList1.forEach(s -> testClassLocal.testIntField += s.length());

		rule.intField = 12;
		rule.testClassField.testIntField = 1;
		for (Map.Entry<String, Map<String, String>> entry : validationConfigurations.entrySet()) {
			final Map<String, String> clone = new HashMap<>(entry.getValue()
				.size());
			entry.getValue()
				.entrySet()
				.forEach(entry2 -> clone.put(entry2.getKey(), entry2.getValue()));
			rule.validationConfigurations.put(entry.getKey(), clone);
		}
		rule.intField = 12;
		rule.testClassField.testIntField = 1;
		rule = null;

		StringBuilder sb = new StringBuilder();
		stringList1.forEach(s -> {
			sb.append(s);
			stringList2.forEach(n -> {
				sb.append(n + ",");
				stringList3.forEach(r -> {
					final String t = s;
					sb.append(r + t);
				});
			});
		});

		for (String s : stringList1) {
			s += ";";
			sb.append(s);
			for (String n : stringList2) {
				sb.append(n + ",");
				for (String r : stringList3) {
					final String t = s;
					sb.append(r + t);
				}
			}
		}

		final int testClassStringListSize = testClassLocal.stringList.size();
		final LinkedList<String> stringListLocal = new LinkedList<>();
		for (int i = 0; i < testClassStringListSize; i++) {
			for (String s : stringListLocal) {
				if (StringUtils.contains(testClassLocal.stringList.get(i), s)) {
					testClassLocal.stringList.get(i);
					testClassLocal.stringList.get(i);
				}
			}
		}

		/*
		 * SIM-472 bugfix
		 */
		stringList1.forEach(s -> {
			final TestClass tc = new TestClass();
		});

		for (String s : stringList1) {
			final TestClass tc = new TestClass(1);
		}

		for (Object o : (List) stringList1) {
			logger.info(String.valueOf(o));
		}

		return "";
	}

	public void rawIterator() {
		final List<Class> classes = stringList1.stream()
			.map(String::getClass)
			.collect(Collectors.toList());
		for (Class clazz : classes) {
			logger.info(String.valueOf(clazz));
		}
	}

	public void captureTypeIterator() {
		final List<List<? extends Person>> persons = new ArrayList<>();
		for (List<? extends Person> clazz : persons) {
			logger.info(String.valueOf(clazz));
		}
	}

	public void wildCardTypeIterator() {
		final List<List<? extends Person>> persons = new ArrayList<>();
		for (Class clazz : getWildCardSet()) {
			logger.info(String.valueOf(clazz));
		}
	}

	public Set<Class<?>> getWildCardSet() {
		return null;
	}

	private void useClass(Class<Object> c) {
		c.getAnnotations();
	}

	private void collectionOfDoubles() {
		final List<Double> doubles = new ArrayList<>();

		doubles.stream()
			.mapToDouble(Double::valueOf)
			.forEach(d -> {
				final double halfD = d / 2;
				logger.info(String.valueOf(halfD + d));
			});
	}

	private void collectionOfInts() {
		final List<Integer> doubles = new ArrayList<>();
		doubles.stream()
			.mapToInt(Integer::valueOf)
			.forEach(i -> {
				final int plusTwo = i + 2;
				logger.info(String.valueOf(plusTwo + i));
			});
	}

	private void collectionOfLongs() {
		final List<Long> longs = new ArrayList<>();
		longs.stream()
			.mapToLong(Long::valueOf)
			.forEach(l -> {
				final long minusTwo = l - 2;
				logger.info(String.valueOf(minusTwo + l));
			});
	}

	private void boxedIteratingVariable() {
		final List<Double> doubles = new ArrayList<>();
		doubles.forEach(d -> {
			final double halfD = d / 2;
			logger.info(String.valueOf(halfD + d));
		});
	}

	private class TestClass {
		public int testIntField = 0;
		public List<String> stringList = Arrays.asList("asdf", "jkl");

		public TestClass() {

		}

		public TestClass(int testIntField) throws FileNotFoundException {
			throw new FileNotFoundException();
		}
	}

	private class Employee extends Person {

		public Employee(String name, LocalDate birthday) {
			super(name, birthday);
		}

	}
}
