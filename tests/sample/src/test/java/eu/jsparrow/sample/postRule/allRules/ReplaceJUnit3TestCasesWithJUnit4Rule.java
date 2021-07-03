package eu.jsparrow.sample.postRule.allRules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceJUnit3TestCasesWithJUnit4Rule {

	@BeforeEach
	protected void setUp() {
	}

	@AfterEach
	protected void tearDown() {
	}

	@Test
	public void test() throws Exception {
		assertEquals(0x7fffffff, Integer.MAX_VALUE);
	}

	@Test
	public void testAssertEqualsWithMessage() throws Exception {
		assertEquals(0x7fffffff, Integer.MAX_VALUE, "Expected to be {0x7fffffff}.");
	}
}