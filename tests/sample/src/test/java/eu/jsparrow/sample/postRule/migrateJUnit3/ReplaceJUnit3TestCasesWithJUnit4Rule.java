package eu.jsparrow.sample.postRule.migrateJUnit3;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;

public class ReplaceJUnit3TestCasesWithJUnit4Rule extends TestCase {

	@Before
	@Override
	protected void setUp() {
	}

	@After
	@Override
	protected void tearDown() {
	}

	@Test
	public void test() throws Exception {
		Assert.assertEquals(0x7fffffff, Integer.MAX_VALUE);
	}

	@Test
	public void testAssertEqualsWithMessage() throws Exception {
		Assert.assertEquals("Expected to be {0x7fffffff}.", 0x7fffffff, Integer.MAX_VALUE);
	}
}