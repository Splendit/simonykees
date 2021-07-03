package eu.jsparrow.sample.postRule.migrateJUnit3;

import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

public class ReplaceJUnit3TestCasesWithJupiterRule {

	@BeforeEach
	protected void setUp() {
	}

	@AfterEach
	protected void tearDown() {
	}

	@Test
	public void test() throws Exception {
		Assertions.assertEquals(0x7fffffff, Integer.MAX_VALUE);
	}

	@Test
	public void testAssertEqualsWithMessage() throws Exception {
		Assertions.assertEquals(0x7fffffff, Integer.MAX_VALUE, "Expected to be {0x7fffffff}.");
	}
}