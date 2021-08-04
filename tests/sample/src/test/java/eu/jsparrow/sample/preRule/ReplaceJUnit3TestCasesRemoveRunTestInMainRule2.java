package eu.jsparrow.sample.preRule;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import eu.jsparrow.sample.utilities.UnexpectedTestCase;

public class ReplaceJUnit3TestCasesRemoveRunTestInMainRule2 extends TestCase {

	public void testAddition() {
		assertEquals(10, 5 + 5);
	}

	public static void main(String[] args) {
		String helloWorld = "Hello World!";
		TestRunner.run(UnexpectedTestCase.class);
	}
}