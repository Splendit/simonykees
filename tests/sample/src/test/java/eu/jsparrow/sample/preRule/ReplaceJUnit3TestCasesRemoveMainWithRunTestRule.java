package eu.jsparrow.sample.preRule;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class ReplaceJUnit3TestCasesRemoveMainWithRunTestRule extends TestCase {

	public void testAddition() {
		assertEquals(10, 5 + 5);
	}

	public static void main(String[] args) {
		TestRunner.run(ReplaceJUnit3TestCasesRemoveMainWithRunTestRule.class);
	}
}