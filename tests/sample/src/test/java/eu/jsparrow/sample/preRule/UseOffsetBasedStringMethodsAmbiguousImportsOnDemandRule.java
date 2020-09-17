package eu.jsparrow.sample.preRule;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.*;
import static java.lang.Math.*;

public class UseOffsetBasedStringMethodsAmbiguousImportsOnDemandRule {

	int abs =  abs(-1);

	public int testIndexOfCharacterD(String str) {
		max();
		return str.substring(6)
			.indexOf('D');
	}
}