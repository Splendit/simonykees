package eu.jsparrow.sample.preRule;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class ReplaceJUnit3TestCasesRunUnexpectedTestInMainRule extends TestCase {

	public void testAddition() {
		assertEquals(10, 5 + 5);
	}

	public static void main(String[] args) {
		String helloWorld = "Hello World!";
		TestRunner.run(eu.jsparrow.sample.utilities.UnexpectedTestCase.class);
	}
}