package eu.jsparrow.sample.preRule;

public class TestUseTernaryOperatorRule {

	void exampleWithAssignment(boolean condition) {
		int x;
		if (condition) {
			x = 1;
		} else {
			x = 0;
		}
	}

	int exampleWithReturn(boolean condition) {
		if (condition) {
			return 1;
		} else {
			return 0;
		}
	}
}
