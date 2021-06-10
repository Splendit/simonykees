package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UseDedicatedAssertionsComparingWithFinalFieldRule {

	final String helloWorld = "Hello World!";

	String s;

	@Test
	public void testHelloWorldWithQualifier() {
		assertTrue(s.equals(this.helloWorld));
	}
}