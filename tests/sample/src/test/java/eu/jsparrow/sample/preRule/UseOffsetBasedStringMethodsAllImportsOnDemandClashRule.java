package eu.jsparrow.sample.preRule;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.*;
import eu.jsparrow.sample.utilities.*;

public class UseOffsetBasedStringMethodsAllImportsOnDemandClashRule {

	TestModifier testModifier;

	public int testIndexOfCharacterD(String str) {
		max();
		return str.substring(6)
			.indexOf('D');
	}
}