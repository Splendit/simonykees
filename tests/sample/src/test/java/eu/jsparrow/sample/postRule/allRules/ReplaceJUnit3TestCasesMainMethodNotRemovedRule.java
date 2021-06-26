package eu.jsparrow.sample.postRule.allRules;

import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesMainMethodNotRemovedRule extends TestCase {

	public static void main(String[] args) {

	}

	void useMain() {
		main(new String[] {});
	}
}