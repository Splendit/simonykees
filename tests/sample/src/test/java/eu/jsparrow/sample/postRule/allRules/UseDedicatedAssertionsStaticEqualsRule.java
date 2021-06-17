package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class UseDedicatedAssertionsStaticEqualsRule {

	@Test
	public void test() {
		assertTrue(TestWithStaticEquals.equals(1));
	}

	static class TestWithStaticEquals {

		static boolean equals(int x) {
			return true;
		}
	}
}