package eu.jsparrow.sample.preRule;

import eu.jsparrow.sample.utilities.ClassUsingJUnit3TestResult;
import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesUsingJUnit3TestResultGetterRule extends TestCase {

	public void test() {
		assertNotNull(new ClassUsingJUnit3TestResult().getTestResult());
	}
}