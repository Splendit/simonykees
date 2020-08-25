package eu.jsparrow.sample.postRule.useOffsetBasedStringMethods;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.max;

public class UseOffsetBasedStringMethodsImportMaxClashRule {

	public int testIndexOfCharacterD(String str) {
		max();
		return Math.max(str
			.indexOf('D', 6) - 6, -1);
	}
}