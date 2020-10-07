package eu.jsparrow.sample.preRule;

public class UseOffsetBasedStringMethodsMathAsVariableRule {

	void testWithVariableMath() {
		String Math = "";
		String str = "Hello World!";
		int index = str.substring(6)
			.indexOf('d');
	}

	void testWithoutVariableMath() {
		String str = "Hello World!";
		int index = str.substring(6)
			.indexOf('d');
	}

	void max() {
	}
}