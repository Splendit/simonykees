package eu.jsparrow.sample.utilities;

import junit.framework.TestResult;

public class ClassExtendingJUnit3TestResult extends TestResult {
	public static final int MAX_INT = Integer.MAX_VALUE;

	public static final int getMaxInt() {
		return MAX_INT;
	}
}
