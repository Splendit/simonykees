package eu.jsparrow.sample.preRule;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class UseDedicatedAssertionsStaticEqualsRule {
	
	static class TestWithStaticEquals {

		static boolean equals(int x) {
			return true;
		}	
	}
	
	@Test
	public void test() {
		assertTrue(TestWithStaticEquals.equals(1));
	}
}