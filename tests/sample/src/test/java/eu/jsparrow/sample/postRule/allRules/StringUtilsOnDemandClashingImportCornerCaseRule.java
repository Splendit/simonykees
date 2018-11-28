package eu.jsparrow.sample.postRule.allRules;

import java.util.List;

import eu.jsparrow.sample.utilities.NumberUtils;

/**
 * Having imported a StringUtils class from any package, no other StringUtils
 * class should be imported. Therefore, in such cases, the StringUtilsRule
 * cannot be applied.
 *
 */
@SuppressWarnings("nls")
public class StringUtilsOnDemandClashingImportCornerCaseRule {

	public int testIndexOf(String testString) {
		NumberUtils numUtil;
		NumberUtils.explode();
		List<String> list;
		return testString.indexOf("e");
	}

	public boolean testEmpty(String testString) {
		return testString.isEmpty();
	}

}
