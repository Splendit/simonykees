package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UseDedicatedAssertionsRule {

	@Test
	public void test() {
		Object a = new Object();
		Object b = a;
		assertTrue(a.equals(b));
	}
}