package at.splendit.simonykees.sample.postRule.allRules;

import at.splendit.simonykees.sample.utilities.NumberUtils;
import at.splendit.simonykees.sample.utilities.StringUtils;

public class OrganiseConflictingImportsRule {

	public void useLocalStringUtils(String input) {
		StringUtils.doesntDoAnything();
		NumberUtils.explode();

	}

}
