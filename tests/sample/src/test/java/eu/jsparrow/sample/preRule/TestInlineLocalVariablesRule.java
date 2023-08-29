package eu.jsparrow.sample.preRule;

public class TestInlineLocalVariablesRule {
	int result;

	void exampleWithAssignment() {
		int x = 1;
		int y = x;
		result = y;
	}

	int exampleWithReturn(boolean condition) {
		int x = 1;
		int y = x;
		return y;
	}
}
