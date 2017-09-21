package eu.jsparrow.sample.preRule;

import java.lang.reflect.*;
import java.math.BigDecimal;

import eu.jsparrow.sample.utilities.NumberUtils;
import eu.jsparrow.sample.utilities.StringUtils;

public class OrganiseConflictingImportsRule {
	
	public void useLocalStringUtils(String input) {
		StringUtils.doesntDoAnything();
		NumberUtils.explode();
		
	}

}
