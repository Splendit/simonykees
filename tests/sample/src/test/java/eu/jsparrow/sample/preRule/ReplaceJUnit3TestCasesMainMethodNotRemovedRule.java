package eu.jsparrow.sample.preRule;

import junit.framework.TestCase;

public class ReplaceJUnit3TestCasesMainMethodNotRemovedRule extends TestCase {

	public static void main(String[] args) {

	}

	void useMain() {
		main(new String[] {});
	}
}