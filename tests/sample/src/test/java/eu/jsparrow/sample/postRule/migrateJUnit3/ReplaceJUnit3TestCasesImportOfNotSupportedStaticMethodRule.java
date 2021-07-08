package eu.jsparrow.sample.postRule.migrateJUnit3;

import static eu.jsparrow.sample.utilities.ClassExtendingJUnit3TestResult.getMaxInt;
import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesImportOfNotSupportedStaticMethodRule extends TestCase {

	public void test() {
		assertEquals(Integer.MAX_VALUE, getMaxInt());
	}
}