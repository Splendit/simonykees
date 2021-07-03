package eu.jsparrow.sample.postRule.migrateJUnit3;

import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;

public class ReplaceJUnit3TestCasesWithJUnit4Rule {

	@Before
	protected void setUp() {
	}

	@After
	protected void tearDown() {
	}

	@Test
	public void test() throws Exception {
		assertEquals(0x7fffffff, Integer.MAX_VALUE);
	}

	@Test
	public void testAssertEqualsWithMessage() throws Exception {
		assertEquals("Expected to be {0x7fffffff}.", 0x7fffffff, Integer.MAX_VALUE);
	}
}