package eu.jsparrow.sample.postRule.allRules;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.max;

import org.apache.commons.lang3.StringUtils;

public class UseOffsetBasedStringMethodsImportMaxClashRule {

	public int testIndexOfCharacterD(String str) {
		max();
		return Math.max(StringUtils.indexOf(str, 'D', 6) - 6, -1);
	}
}