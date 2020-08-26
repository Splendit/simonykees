package eu.jsparrow.sample.postRule.useOffsetBasedStringMethods;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.*;
import static java.lang.Math.*;

public class UseOffsetBasedStringMethodsAmbiguousImportsOnDemandRule {

	int abs =  abs(-1);

	public int testIndexOfCharacterD(String str) {
		max();
		return Math.max(str
			.indexOf('D', 6) - 6, -1);
	}
}