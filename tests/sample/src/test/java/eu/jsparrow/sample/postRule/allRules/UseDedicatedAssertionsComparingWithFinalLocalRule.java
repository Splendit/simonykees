package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class UseDedicatedAssertionsComparingWithFinalLocalRule {

	@Test
	public void test() {
		final Object o = new Object();
		final Object oFinal = new Object();
		assertNotEquals(o, oFinal);
	}
}