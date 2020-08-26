package eu.jsparrow.sample.postRule.useOffsetBasedStringMethods;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.*;
import static eu.jsparrow.sample.utilities.InnerClasses.*;

public class UseOffsetBasedStringMethodsAllImportsOnDemandClash2Rule {

	OtherInnerClass otherInnerClass;
	
	public int testIndexOfCharacterD(String str) {
		max();
		return java.lang.Math.max(str
			.indexOf('D', 6) - 6, -1);
	}
}