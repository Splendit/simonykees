package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UseDedicatedAssertionsComparingWithFinalFieldRule {

	final String helloWorld = "Hello World!";

	String s;

	@Test
	public void testHelloWorldWithQualifier() {
		assertEquals(this.helloWorld, s);
	}
}