package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class UseDedicatedAssertionsComparingWithFinalLocalRule {

	@Test
	public void test() {
		Object o = new Object();
		final Object oFinal = new Object();
		assertFalse(o.equals(oFinal));
	}
}