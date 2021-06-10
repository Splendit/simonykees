package eu.jsparrow.sample.postRule.dedicatedAssertions;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import static org.junit.Assert.assertNotEquals;

public class UseDedicatedAssertionsComparingWithFinalLocalRule {

	@Test
	public void test() {
		Object o = new Object();
		final Object oFinal = new Object();
		assertNotEquals(oFinal, o);
	}
}