package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UseDedicatedAssertionsComparingWithConstantRule {

	public static final String HELLO_WORLD = "Hello World!";

	String s;

	@Test
	public void testHelloWorldWithoutQualifier() {
		assertEquals(HELLO_WORLD, s);
	}

	@Test
	public void testHelloWorldWithQualifier() {
		assertEquals(UseDedicatedAssertionsComparingWithConstantRule.HELLO_WORLD, s);
	}
}