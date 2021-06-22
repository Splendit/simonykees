package eu.jsparrow.sample.postRule.allRules;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesWithJupiterRule extends TestCase {

	@BeforeEach
	@Override
	protected void setUp() {
	}

	@AfterEach
	@Override
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