package eu.jsparrow.sample.postRule.useOffsetBasedStringMethods;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.max;
import eu.jsparrow.sample.utilities.Math;

public class UseOffsetBasedStringMethodsAllImportsClashRule {
	Math math;
	public int testIndexOfCharacterD(String str) {
		max();
		return java.lang.Math.max(str
			.indexOf('D', 6) - 6, -1);
	}
}