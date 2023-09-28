package eu.jsparrow.sample.postRule.useTernaryOperator;

public class TestUseTernaryOperatorRule {

	void exampleWithAssignment(boolean condition) {
		int x = condition ? 1 : 0;
	}

	int exampleWithReturn(boolean condition) {
		return condition ? 1 : 0;
	}
}
