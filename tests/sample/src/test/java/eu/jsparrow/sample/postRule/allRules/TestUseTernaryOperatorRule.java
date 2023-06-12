package eu.jsparrow.sample.postRule.allRules;

public class TestUseTernaryOperatorRule {

	void exampleWithAssignment(boolean condition) {
		final int x = condition ? 1 : 0;
	}

	int exampleWithReturn(boolean condition) {
		return condition ? 1 : 0;
	}
}
