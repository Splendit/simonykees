package eu.jsparrow.sample.postRule.dedicatedAssertions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

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