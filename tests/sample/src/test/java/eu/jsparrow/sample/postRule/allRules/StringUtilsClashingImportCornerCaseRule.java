package eu.jsparrow.sample.postRule.allRules;

import java.util.List;

import eu.jsparrow.sample.utilities.NumberUtils;
import eu.jsparrow.sample.utilities.StringUtils;

/**
 * Having imported a StringUtils class from any package, no other StringUtils
 * class should be imported. Therefore, in such cases, the StringUtilsRule
 * cannot be applied.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@SuppressWarnings("nls")
public class StringUtilsClashingImportCornerCaseRule {

	public int testIndexOf(String testString) {
		final StringUtils stringUtils;
		StringUtils.doesntDoAnything();
		final NumberUtils numUtil;
		NumberUtils.explode();
		final List<String> list;
		return testString.indexOf("e");
	}
}
