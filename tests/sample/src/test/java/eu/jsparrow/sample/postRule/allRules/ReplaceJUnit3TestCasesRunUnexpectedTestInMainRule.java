package eu.jsparrow.sample.postRule.allRules;

import eu.jsparrow.sample.utilities.UnexpectedTestCase;
import junit.framework.TestCase;
import junit.textui.TestRunner;

public class ReplaceJUnit3TestCasesRunUnexpectedTestInMainRule extends TestCase {

	public void testAddition() {
		assertEquals(10, 5 + 5);
	}

	public static void main(String[] args) {
		final String helloWorld = "Hello World!";
		TestRunner.run(UnexpectedTestCase.class);
	}
}