package eu.jsparrow.sample.postRule.allRules;

public class TestInlineLocalVariablesRule {
	int result;

	void exampleWithAssignment() {
		final int y = 1;
		result = y;
	}

	int exampleWithReturn(boolean condition) {
		final int y = 1;
		return y;
	}
}
