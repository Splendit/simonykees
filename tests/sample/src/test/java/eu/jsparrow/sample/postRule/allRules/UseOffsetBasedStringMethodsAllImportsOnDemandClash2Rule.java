package eu.jsparrow.sample.postRule.allRules;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.max;

import org.apache.commons.lang3.StringUtils;

import eu.jsparrow.sample.utilities.ClassWithStaticInnerClasses.OtherInnerClass;

public class UseOffsetBasedStringMethodsAllImportsOnDemandClash2Rule {

	OtherInnerClass otherInnerClass;

	public int testIndexOfCharacterD(String str) {
		max();
		return java.lang.Math.max(StringUtils.indexOf(str, 'D', 6) - 6, -1);
	}
}