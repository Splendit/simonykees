package eu.jsparrow.sample.postRule.allRules;

import org.apache.commons.lang3.StringUtils;

public class UseOffsetBasedStringMethodsMathAsVariableRule {

	void testWithVariableMath() {
		final String Math = "";
		final String str = "Hello World!";
		final int index = java.lang.Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
	}

	void testWithoutVariableMath() {
		final String str = "Hello World!";
		final int index = Math.max(StringUtils.indexOf(str, 'd', 6) - 6, -1);
	}

	void max() {
	}
}