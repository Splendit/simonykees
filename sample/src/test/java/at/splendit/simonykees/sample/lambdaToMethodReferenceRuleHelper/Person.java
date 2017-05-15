package at.splendit.simonykees.sample.lambdaToMethodReferenceRuleHelper;

import java.time.LocalDate;

public class Person {
	private String name;
	private LocalDate birthday;

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
}
