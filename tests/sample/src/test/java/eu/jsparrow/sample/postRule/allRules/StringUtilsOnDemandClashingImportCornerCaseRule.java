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
		final NumberUtils numUtil;
		NumberUtils.explode();
		final List<String> list;
		return org.apache.commons.lang3.StringUtils.indexOf(testString, "e");
	}

	public boolean testEmpty(String testString) {
		return org.apache.commons.lang3.StringUtils.isEmpty(testString);
	}

}
