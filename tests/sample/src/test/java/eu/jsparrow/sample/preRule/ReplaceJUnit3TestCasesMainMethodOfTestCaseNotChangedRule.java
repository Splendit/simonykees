package eu.jsparrow.sample.preRule;

import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesMainMethodOfTestCaseNotChangedRule extends TestCase {

	public void test() throws Exception {
		assertEquals(0x7fffffff, Integer.MAX_VALUE);
	}
	
	public static void methodCalledInMain() {
		
	}

	public static void main(String[] args) {
		methodCalledInMain();
	}
}