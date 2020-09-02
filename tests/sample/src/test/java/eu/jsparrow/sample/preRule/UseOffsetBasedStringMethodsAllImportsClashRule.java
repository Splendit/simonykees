package eu.jsparrow.sample.preRule;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.max;
import eu.jsparrow.sample.utilities.Math;

public class UseOffsetBasedStringMethodsAllImportsClashRule {

	Math math;

	public int testIndexOfCharacterD(String str) {
		max();
		return str.substring(6)
			.indexOf('D');
	}
}