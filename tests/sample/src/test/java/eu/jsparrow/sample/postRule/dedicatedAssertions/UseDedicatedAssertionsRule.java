package eu.jsparrow.sample.postRule.dedicatedAssertions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class UseDedicatedAssertionsRule {

	@Test
	public void test() {
		Object a = new Object();
		Object b = a;
		assertEquals(a, b);
	}
}