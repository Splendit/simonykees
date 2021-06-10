package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UseDedicatedAssertionsComparingWithConstantRule {

	public static final String HELLO_WORLD = "Hello World!";

	String s;

	@Test
	public void testHelloWorldWithoutQualifier() {
		assertTrue(s.equals(HELLO_WORLD));
	}

	@Test
	public void testHelloWorldWithQualifier() {
		assertTrue(s.equals(UseDedicatedAssertionsComparingWithConstantRule.HELLO_WORLD));
	}
}