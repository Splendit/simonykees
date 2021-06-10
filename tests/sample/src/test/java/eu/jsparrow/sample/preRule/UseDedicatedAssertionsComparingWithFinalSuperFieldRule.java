package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UseDedicatedAssertionsComparingWithFinalSuperFieldRule {

	final String helloWorld = "Hello World!";

	String s;

	class ChildClass extends UseDedicatedAssertionsComparingWithFinalSuperFieldRule {

		@Test
		public void testHelloWorldWithQualifier() {
			assertTrue(s.equals(super.helloWorld));
		}
	}
}