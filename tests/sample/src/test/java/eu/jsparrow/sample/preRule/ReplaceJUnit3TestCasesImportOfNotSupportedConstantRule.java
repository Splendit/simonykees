package eu.jsparrow.sample.preRule;

import static eu.jsparrow.sample.utilities.ClassExtendingJUnit3TestResult.MAX_INT;
import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesImportOfNotSupportedConstantRule extends TestCase {

	public void test() {
		assertEquals(Integer.MAX_VALUE, MAX_INT);
	}
}