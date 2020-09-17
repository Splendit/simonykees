package eu.jsparrow.sample.preRule;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.max;

public class UseOffsetBasedStringMethodsImportMaxClashRule {

	public int testIndexOfCharacterD(String str) {
		max();
		return str.substring(6)
			.indexOf('D');
	}
}