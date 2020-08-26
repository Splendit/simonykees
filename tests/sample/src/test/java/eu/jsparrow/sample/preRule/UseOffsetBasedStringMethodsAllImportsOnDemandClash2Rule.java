package eu.jsparrow.sample.preRule;

import static eu.jsparrow.sample.utilities.ClassWithMaxMethod.*;
import static eu.jsparrow.sample.utilities.InnerClasses.*;

public class UseOffsetBasedStringMethodsAllImportsOnDemandClash2Rule {

	OtherInnerClass otherInnerClass;
	
	public int testIndexOfCharacterD(String str) {
		max();
		return str.substring(6)
			.indexOf('D');
	}
}