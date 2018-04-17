package eu.jsparrow.sample.utilities;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * class is just for test purposes for {@link LambdaToMethodReferenceRule}
 * 
 * @author Matthias Webhofer
 * @since 1.2
 *
 */
public class Person {
	private String name;
	private LocalDate birthday;
	private Person parent1;
	private Person parent2;

	public Person(String name, LocalDate birthday) {
		this.name = name;
		this.birthday = birthday;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public static int compareByAge(Person a, Person b) {
		return a.birthday.compareTo(b.birthday);
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Person getParent1() {
		return parent1;
	}

	public void setParent1(Person parent1) {
		this.parent1 = parent1;
	}

	public Person getParent2() {
		return parent2;
	}

	public void setParent2(Person parent2) {
		this.parent2 = parent2;
	}

	public void doSomething(Person p) {

	}

	public static boolean filter(Predicate<? extends TestModifier> predicate) {
		return true;
	}
}
