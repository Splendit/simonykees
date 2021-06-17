package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UseDedicatedAssertionsRule {

	@Test
	public void test() {
		final Object a = new Object();
		final Object b = a;
		assertEquals(a, b);
	}
}