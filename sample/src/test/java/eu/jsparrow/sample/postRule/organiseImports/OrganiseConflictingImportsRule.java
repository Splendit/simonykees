package eu.jsparrow.sample.postRule.organiseImports;

import eu.jsparrow.sample.utilities.NumberUtils;
import eu.jsparrow.sample.utilities.StringUtils;

public class OrganiseConflictingImportsRule {
	
	public void useLocalStringUtils(String input) {
		StringUtils.doesntDoAnything();
		NumberUtils.explode();
		
	}

}
