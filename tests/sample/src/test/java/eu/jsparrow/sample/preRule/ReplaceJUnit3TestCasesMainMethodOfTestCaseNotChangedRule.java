package eu.jsparrow.sample.preRule;

import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesMainMethodOfTestCaseNotChangedRule extends TestCase {

	public void test() throws Exception {
		assertEquals(0x7fffffff, Integer.MAX_VALUE);
	}

	private static void notRunningTestCase() {
	}

	private static boolean notCalledWithinStatement() {
		return true;
	}

	private static boolean notCalledWithinBlockStatement() {
		return true;
	}

	public static void main(String[] args) {
		if (notCalledWithinStatement())
			notCalledWithinBlockStatement();
		notRunningTestCase();
		ClassWithRunMethods.run();
		ClassWithRunMethods.run("ReplaceJUnit3TestCasesMainMethodOfTestCaseNotChangedRule");
		ClassWithRunMethods.run(ReplaceJUnit3TestCasesMainMethodOfTestCaseNotChangedRule.class);
	}

	private static class ClassWithRunMethods {
		private static void run() {
		}

		private static void run(String testName) {
		}

		private static <T> void run(Class<T> testName) {
		}
	}
}