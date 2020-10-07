package eu.jsparrow.sample.postRule.useOffsetBasedStringMethods;

public class UseOffsetBasedStringMethodsMathAsVariableRule {

	void testWithVariableMath() {
		String Math = "";
		String str = "Hello World!";
		int index = java.lang.Math.max(str
			.indexOf('d', 6) - 6, -1);
	}

	void testWithoutVariableMath() {
		String str = "Hello World!";
		int index = Math.max(str
			.indexOf('d', 6) - 6, -1);
	}

	void max() {
	}
}