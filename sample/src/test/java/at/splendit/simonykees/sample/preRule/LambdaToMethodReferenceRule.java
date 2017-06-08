package at.splendit.simonykees.sample.preRule;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import at.splendit.simonykees.sample.utilities.Person;

/**
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
@SuppressWarnings({ "nls", "unused", "unchecked", "rawtypes" })
public class LambdaToMethodReferenceRule {

	List<LocalDate> dateList = Arrays.asList(LocalDate.of(1992, 1, 1), LocalDate.of(2001, 2, 3),
			LocalDate.of(2010, 10, 10), LocalDate.of(2017, 5, 15));

	List<Person> personList = Arrays.asList(new Person("asdf", LocalDate.of(1999, 1, 1)),
			new Person("jkl", LocalDate.of(2009, 2, 2)), new Person("yxcv", LocalDate.of(1989, 1, 1)));

	public void referenceToStaticMethod() {
		Collections.sort(personList, (Person a, Person b) -> {
			return Person.compareByAge(a, b);
		});

		Collections.sort(personList, (a, b) -> {
			return Person.compareByAge(a, b);
		});

		Collections.sort(personList, (Person a, Person b) -> Person.compareByAge(a, b));

		Collections.sort(personList, (a, b) -> Person.compareByAge(a, b));

		Collections.sort(personList, Person::compareByAge);

		personList.forEach(element -> {
			System.out.println(element);
		});

		personList.forEach(element -> System.out.println(element));

		personList.forEach(System.out::println);

		Collections.sort(personList, (Person a, Person b) -> {
			return Person.compareByAge(a.getParent2(), b);
		});

		Collections.sort(personList, (a, b) -> {
			return Person.compareByAge(a, b.getParent1());
		});

		Collections.sort(personList, (Person a, Person b) -> Person.compareByAge(a, b.getParent2()));

		Collections.sort(personList, (a, b) -> Person.compareByAge(a.getParent1(), b));
	}

	public void referenceToInstanceMethod() {
		ComparisonProvider comparisonProvider = new ComparisonProvider();

		Collections.sort(personList, (Person a, Person b) -> {
			return comparisonProvider.compareByName(a, b);
		});

		Collections.sort(personList, (a, b) -> {
			return comparisonProvider.compareByName(a, b);
		});

		Collections.sort(personList, (Person a, Person b) -> comparisonProvider.compareByName(a, b));

		Collections.sort(personList, (a, b) -> comparisonProvider.compareByName(a, b));

		Collections.sort(personList, comparisonProvider::compareByName);

		Collections.sort(personList, (Person a, Person b) -> {
			return comparisonProvider.compareByName(a.getParent2(), b);
		});

		Collections.sort(personList, (a, b) -> {
			return comparisonProvider.compareByName(a, b.getParent1());
		});

		Collections.sort(personList, (Person a, Person b) -> comparisonProvider.compareByName(a, b.getParent2()));

		Collections.sort(personList, (a, b) -> comparisonProvider.compareByName(a.getParent1(), b));
	}

	public void referenceToLocalMethod() {
		personList.forEach((Person person) -> {
			doSomething(person);
		});

		personList.forEach(person -> {
			doSomething(person);
		});

		personList.forEach((Person person) -> {
			this.doSomething(person);
		});

		personList.forEach(person -> {
			this.doSomething(person);
		});

		personList.forEach((Person person) -> doSomething(person));

		personList.forEach(person -> doSomething(person));

		personList.forEach((Person person) -> this.doSomething(person));

		personList.forEach(person -> this.doSomething(person));

		personList.forEach((Person person) -> getRandomPerson().doSomething(person));

		personList.forEach(person -> getRandomPerson().doSomething(person));

		personList.forEach((Person person) -> this.getRandomPerson().doSomething(person));

		personList.forEach(person -> this.getRandomPerson().doSomething(person));
	}

	public void referenceToInstanceMethodOfArbitraryType() {
		String[] stringArray = { "Barbara", "James", "Mary", "John", "Patricia", "Robert", "Michael", "Linda" };

		Arrays.sort(stringArray, (String a, String b) -> {
			return a.compareToIgnoreCase(b);
		});

		Arrays.sort(stringArray, (a, b) -> {
			return a.compareToIgnoreCase(b);
		});

		Arrays.sort(stringArray, (String a, String b) -> a.compareToIgnoreCase(b));

		Arrays.sort(stringArray, (a, b) -> a.compareToIgnoreCase(b));

		Arrays.sort(stringArray, String::compareToIgnoreCase);
	}

	public void referenceToConstructor() {
		Set<Person> persSet1 = transferElements(personList, () -> {
			return new HashSet<>();
		});

		Set<Person> persSet2 = transferElements(personList, () -> new HashSet<>());

		Set<Person> persSet3 = transferElements(personList, () -> new HashSet());

		Set<Person> persSet4 = transferElements(personList, () -> new HashSet<Person>());

		Set<Person> persSet5 = transferElements(personList, HashSet<Person>::new);
	}

	/*
	 * test cases for SIM-455 bugfix IllegalArgumentException with parameterized type
	 */
	public void referenceToParameterizedType() {
		Map<String, String> map = new HashMap<>();

		map.entrySet().stream().forEach(element -> element.getValue());

		map.entrySet().stream().forEach((Entry element) -> element.getValue());

		map.entrySet().stream().forEach((Entry<String, String> element) -> element.getValue());

		map.entrySet().stream().forEach(Entry<String, String>::getValue);

		map.entrySet().stream().forEach(Entry::getValue);
	}

	class ComparisonProvider {
		public int compareByName(Person a, Person b) {
			return a.getName().compareTo(b.getName());
		}

		public int compareByAge(Person a, Person b) {
			return a.getBirthday().compareTo(b.getBirthday());
		}
	}

	public static <T, SOURCE extends Collection<T>, DEST extends Collection<T>> DEST transferElements(
			SOURCE sourceCollection, Supplier<DEST> collectionFactory) {

		DEST result = collectionFactory.get();
		for (T t : sourceCollection) {
			result.add(t);
		}
		return result;
	}

	private void doSomething(Object o) {

	}

	private Person getRandomPerson() {
		return new Person("Random Person", LocalDate.of(1995, 8, 1));
	}
}
