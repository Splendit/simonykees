package eu.jsparrow.sample.postRule.allRules;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.max;

import eu.jsparrow.sample.utilities.TestModifier;

public class UseOffsetBasedStringMethodsAllImportsOnDemandClashRule {

	TestModifier testModifier;

	public int testIndexOfCharacterD(String str) {
		max();
		return java.lang.Math.max(str.indexOf('D', 6) - 6, -1);
	}
}