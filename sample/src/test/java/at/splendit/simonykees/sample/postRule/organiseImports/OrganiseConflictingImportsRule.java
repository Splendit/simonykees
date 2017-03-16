package at.splendit.simonykees.sample.postRule.organiseImports;

import java.lang.reflect.*;
import java.math.BigDecimal;

import at.splendit.simonykees.sample.utilities.NumberUtils;
import at.splendit.simonykees.sample.utilities.StringUtils;

public class OrganiseConflictingImportsRule {
	
	public void useLocalStringUtils(String input) {
		StringUtils.doesntDoAnything();
		NumberUtils.explode();
		
	}

}
