package eu.jsparrow.sample.postRule.allRules;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.max;

import eu.jsparrow.sample.utilities.TestModifier;

public class UseOffsetBasedStringMethodsAllImportsOnDemandClashRule {

	TestModifier testModifier;

	public int testIndexOfCharacterD(String str) {
		max();
		return java.lang.Math.max(org.apache.commons.lang3.StringUtils.indexOf(str, 'D', 6) - 6, -1);
	}
}