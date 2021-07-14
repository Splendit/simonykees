package eu.jsparrow.sample.preRule;

import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesQualifiedNameOfNotSupportedConstantRule extends TestCase {

	public void test() {
		assertEquals(Integer.MAX_VALUE, eu.jsparrow.sample.utilities.ClassExtendingJUnit3TestResult.MAX_INT);
	}
}