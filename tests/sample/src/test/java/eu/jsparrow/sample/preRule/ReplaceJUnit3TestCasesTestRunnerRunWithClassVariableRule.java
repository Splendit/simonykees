package eu.jsparrow.sample.preRule;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class ReplaceJUnit3TestCasesTestRunnerRunWithClassVariableRule extends TestCase {

	public void testAddition() {
		assertEquals(10, 5 + 5);
	}

	public static void main(String[] args) {
		Class<ReplaceJUnit3TestCasesTestRunnerRunWithClassVariableRule> testClass = ReplaceJUnit3TestCasesTestRunnerRunWithClassVariableRule.class;
		TestRunner.run(testClass);
	}
}