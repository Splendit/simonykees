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

	public void useLabels() {

		xLabel1: for (int i = 0; i < 10; i++) {
			if (i == 3) {
				break xLabel1;
			}
		}

		for (int i = 0; i < 5; i++) {
			xLabel2: for (int j = 0; j < 5; j++) {
				continue xLabel2;
			}
		}
	}

	public static void main(String[] args) {

	}
}