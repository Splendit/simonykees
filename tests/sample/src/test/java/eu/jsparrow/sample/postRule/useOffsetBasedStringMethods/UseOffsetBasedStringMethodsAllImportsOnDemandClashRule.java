package eu.jsparrow.sample.postRule.useOffsetBasedStringMethods;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.*;
import eu.jsparrow.sample.utilities.*;

public class UseOffsetBasedStringMethodsAllImportsOnDemandClashRule {

	TestModifier testModifier;

	public int testIndexOfCharacterD(String str) {
		max();
		return java.lang.Math.max(str
			.indexOf('D', 6) - 6, -1);
	}
}