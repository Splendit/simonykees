package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UseDedicatedAssertionsComparingWithFinalSuperFieldRule {

	final String helloWorld = "Hello World!";

	String s;

	class ChildClass extends UseDedicatedAssertionsComparingWithFinalSuperFieldRule {

		@Test
		public void testHelloWorldWithQualifier() {
			assertEquals(super.helloWorld, s);
		}
	}
}