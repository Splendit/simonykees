package eu.jsparrow.sample.postRule.dedicatedAssertions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class UseDedicatedAssertionsComparingWithFinalFieldRule {

	final String helloWorld = "Hello World!";

	String s;

	@Test
	public void testHelloWorldWithQualifier() {
		assertEquals(this.helloWorld, s);
	}
}