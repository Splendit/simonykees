package eu.jsparrow.sample.postRule.inlineLocalVariables;

public class TestInlineLocalVariablesRule {
	int result;

	void exampleWithAssignment() {
		int y = 1;
		result = y;
	}

	int exampleWithReturn(boolean condition) {
		int y = 1;
		return y;
	}
}
