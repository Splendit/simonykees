package eu.jsparrow.sample.preRule;

import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesWithJUnit4Rule extends TestCase {

	@Override
	protected void setUp() {
	}

	@Override
	protected void tearDown() {
	}

	public void test() throws Exception {
		assertEquals(0x7fffffff, Integer.MAX_VALUE);
	}

	public void testAssertEqualsWithMessage() throws Exception {
		assertEquals("Expected to be {0x7fffffff}.", 0x7fffffff, Integer.MAX_VALUE);
	}

	public static void main(String[] args) {

	}
}