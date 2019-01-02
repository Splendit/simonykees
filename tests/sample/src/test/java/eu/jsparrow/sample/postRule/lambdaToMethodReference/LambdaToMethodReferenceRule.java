package eu.jsparrow.sample.postRule.lambdaToMethodReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import eu.jsparrow.sample.utilities.NumberUtils;
import eu.jsparrow.sample.utilities.Person;
import eu.jsparrow.sample.utilities.TestModifier;

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

		/* save me */
		// I don't want to break anything...
		Collections.sort(personList, Person // I don't want to break anything...
::compareByAge);

		/* save me */
		Collections.sort(personList, Person::compareByAge);

		Collections.sort(personList, Person::compareByAge);

		/* 
		 * save me 
		 * */
		personList.forEach(System.out::println);

		personList.forEach(System.out::println);

		personList.forEach(System.out::println);

		Collections.sort(personList, (Person a, Person b) -> {
			return Person.compareByAge(a.getParent2(), b);
		});

		Collections.sort(personList, (a, b) -> {
			return Person.compareByAge(a, b.getParent1());
		});

		Collections.sort(personList, (Person a, Person b) -> Person.compareByAge(a, b.getParent2()));

		Collections.sort(personList, (a, b) -> Person.compareByAge(a.getParent1(), b));

		// SIM-454 bugfix static methods
		personList.stream().filter(LambdaToMethodReferenceRule::isPerson);

		personList.stream().filter(LambdaToMethodReferenceRule::isPerson);

		personList.stream().filter(LambdaToMethodReferenceRule::isPerson);

		personList.stream().filter(LambdaToMethodReferenceRule::isPerson);

		personList.stream().filter(LambdaToMethodReferenceRule::isPerson);
	}

	public void referenceToInstanceMethod() {
		ComparisonProvider comparisonProvider = new ComparisonProvider();

		Collections.sort(personList, (Person a, Person b) -> {
			return comparisonProvider.compareByName(a, b);
		});

		Collections.sort(personList, (a, b) -> {
			return comparisonProvider.compareByName(a, b);
		});

		Collections.sort(personList, comparisonProvider::compareByName);

		Collections.sort(personList, comparisonProvider::compareByName);

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
		personList.forEach(this::doSomething);

		personList.forEach(this::doSomething);

		personList.forEach(this::doSomething);

		personList.forEach(this::doSomething);

		personList.forEach(this::doSomething);

		personList.forEach(this::doSomething);

		personList.forEach(this::doSomething);

		personList.forEach(this::doSomething);

		personList.forEach((Person person) -> getRandomPerson().doSomething(person));

		personList.forEach(person -> getRandomPerson().doSomething(person));

		personList.forEach((Person person) -> this.getRandomPerson().doSomething(person));

		personList.forEach(person -> this.getRandomPerson().doSomething(person));

		setIterator(new Iterator() {

			@Override
			public boolean hasNext() {
				personList.forEach(person -> doSomething(person));
				return false;
			}

			@Override
			public Object next() {
				personList.forEach((Person person) -> doSomething(person));
				return null;
			}
			
		});
		
		new Iterator<Object>() {

			@Override
			public boolean hasNext() {
				for (Person person : personList) {
					doSomething(person);
				}
				return false;
			}

			@Override
			public Object next() {
				for (Person person : personList) {
					doSomething(person);
				}
				return null;
			}
			
		};
	}

	public void referenceToInstanceMethodOfArbitraryType() {
		String[] stringArray = { "Barbara", "James", "Mary", "John", "Patricia", "Robert", "Michael", "Linda" };

		Arrays.sort(stringArray, (String a, String b) -> {
			return a.compareToIgnoreCase(b);
		});

		Arrays.sort(stringArray, (a, b) -> {
			return a.compareToIgnoreCase(b);
		});

		Arrays.sort(stringArray, String::compareToIgnoreCase);

		Arrays.sort(stringArray, String::compareToIgnoreCase);

		Arrays.sort(stringArray, String::compareToIgnoreCase);
	}

	public void referenceToConstructor() {
		Set<Person> persSet1 = transferElements(personList, () -> {
			return new HashSet<>();
		});

		Set<Person> persSet2 = transferElements(personList, HashSet::new);

		Set<Person> persSet3 = transferElements(personList, HashSet::new);

		Set<Person> persSet4 = transferElements(personList, HashSet<Person>::new);
		
		Runnable t = () -> new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
		};

		Set<Person> persSet5 = transferElements(personList, HashSet<Person>::new);

		/*
		 * SIM-464 bugfix
		 */
		List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0);

		list.stream().map((Integer iterator) -> new java.awt.geom.Ellipse2D.Double(iterator, 2.0, 4.0, 4.0));

		list.stream().map(Double::new);

		/*
		 * SIM-532 bugfix
		 */
		personList.stream().map(p -> new Person(p.getName(), p.getBirthday())).forEach(Person::getBirthday);
	}

	/*
	 * test cases for SIM-455 bugfix IllegalArgumentException with parameterized type
	 */
	public void referenceToParameterizedType() {
		Map<String, String> map = new HashMap<>();

		map.entrySet().stream().forEach(Map.Entry::getValue);

		map.entrySet().stream().forEach(Map.Entry::getValue);

		map.entrySet().stream().forEach(Map.Entry::getValue);

		map.entrySet().stream().forEach(Entry<String, String>::getValue);

		map.entrySet().stream().forEach(Entry::getValue);
	}
	
	/*
	 * SIM-523 corner cases
	 */
	public <T> void consumeString(T s) {
		
	}
	
	class NestedClass {
		public void referencingMethodInNestedClass() {
			List<Person> persons = new ArrayList<>();
			persons.stream().map(Person::getName).forEach(name -> consumeString(name));
		}
		
		public <T> T consumeObject() {
			return  null;
		}
	}
	
	public void saveTypeArguments(String input) {
		List<Person> persons = new ArrayList<>();
		persons.stream().map(Person::getName).forEach(this::<String>consumeString);
	}
	
	public void missingTypeArguments3(String input) {
		List<NestedClass> persons = new ArrayList<>();
		persons.stream().map(NestedClass::<String>consumeObject);
	}
	
	public void missingTypeArguments2(String input) {
		List<Person> persons = new ArrayList<>();
		persons.stream().map(Employee<String>::new);
	}
	
	public void missingTypeArguments(String input) {
		List<NestedClass> persons = new ArrayList<>();
		persons.stream().map(NestedClass::consumeObject);
	}
	
	
	public void captureTypes(String input) {
		List<? extends Person> persons = new ArrayList<>();
		List<String> names = persons.stream().map(Person::getName).collect(Collectors.toList());
	}
	
	public void captureOfParameterizedTypes(String input) {
		List<? extends Employee<String>> persons = new ArrayList<>();
		List<String> names = persons.stream().map(Employee::getName).collect(Collectors.toList());
	}

	public void missingImports() {
		Person.filter(TestModifier::isStatic);
	}
	
	/*
	 * SIM-821 - the following should not be changed
	 */
	Function<Integer, String> toString = (Integer i) -> i.toString();
	Function<Integer, String> toStringStatic = (Integer i) -> Integer.toString(i);
	Function<AmbiguousMethods, String> testingAmb = (AmbiguousMethods i) -> AmbiguousMethods.testAmbiguity(i);
	Function<AmbiguousMethods, String> testingAmb2 = (AmbiguousMethods i) -> i.testAmbiguity();
	
	
	public void usingQualifiedName() {
		List<UsingApacheNumberUtils> numberUtils = new ArrayList<>();
		/*
		 * Expecting the transformation to use a fully qualified name. 
		 */
		numberUtils.stream().map(UsingApacheNumberUtils::getNumber).map(org.apache.commons.lang3.math.NumberUtils::toString);
		
		List<List<String>> javaLists = new ArrayList<>();
		javaLists.stream().map(eu.jsparrow.sample.utilities.List::new);
		
		List<eu.jsparrow.sample.utilities.List<String>> customLists = new ArrayList<>();
		customLists.stream().map(eu.jsparrow.sample.utilities.List::size);
	}
	
	public void usingOverloadedMethods(Other other) {
		/*
		 * SIM-1351 The wrap and the other.read methods are both overloaded.
		 * Converting to method reference causes ambiguity.
		 */
		ClassWithStaticOverloadedMethods.wrap(() -> other.read());

		/*
		 * SIM-1351 The other.overloadedWihtPrivateMethod should not cause
		 * ambiguity because the overloaded method is private. It should be
		 * possible to convert to method reference.
		 */
		ClassWithStaticOverloadedMethods.wrap(other::overloadedWihtPrivateMethod);

	}
	
	public void referringToMethodsInRawObjects() {
		/*
		 * SIM-1400
		 */
		Employee employee = new Employee("John", LocalDate.now().minusYears(123));
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(() -> employee.getName());
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

	public static boolean isPerson(Person a) {
		return true;
	}

	private void doSomething(Object o) {

	}

	private Person getRandomPerson() {
		return new Person("Random Person", LocalDate.of(1995, 8, 1));
	}

	private void setIterator(Iterator iterator) {

	}

	class Employee<T> extends Person {
		public Employee(String name, LocalDate birthday) {
			super(name, birthday);
		}
		
		public Employee(Person p) {
			super(p.getName(), p.getBirthday());
		}
		
		@Override
		public String getName() {
			return "e:" + super.getName();
		}
		
	}
	
	class UsingApacheNumberUtils {
		/**
		 * There is already an existing import of another NumberUtils class.
		 * Namely {@link NumberUtils}. Therefore, {@link org.apache.commons.lang3.math.NumberUtils}
		 * has to always use a fully qualified name.
		 */
		public org.apache.commons.lang3.math.NumberUtils getNumber() {
			return null;
		}
	}
}

/**
 *  SIM-821
 */
class AmbiguousMethods {
	
	public String testAmbiguity() {
		return "nonStaticMethod";
	}
	
	public String testAmbiguity(int i) {
		return "nonStaticMethod";
	}
	
	public String testAmbiguity(String s, int i) {
		return "nonStaticMethod";
	}
	
	public static String testAmbiguity(AmbiguousMethods i) {
		return  String.valueOf(i);
	}
}

/**
 * SIM-1351
 */
class Other {
	
	public byte [] read () {
		return new byte[] {};
	}
	
	public String read(boolean bytes) {
		return null;
	}
	
	public byte[] overloadedWihtPrivateMethod() {
		return overloadedWihtPrivateMethod(false);
	}
	
	private byte[] overloadedWihtPrivateMethod(boolean bytes) {
		return new byte[] {};
	}
}

class ClassWithStaticOverloadedMethods {

	public static void wrap(CheckedRunnable runable) {

	}

	public static <T> T wrap(CheckedSupplier<T> block) {
		return block.get();
	}

}

interface CheckedRunnable {
	void run();
}

interface CheckedSupplier<R> {

	R get();
}

