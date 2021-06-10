package eu.jsparrow.sample.postRule.dedicatedAssertions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

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