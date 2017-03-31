package at.splendit.simonykees.sample.postRule.allRules;

import java.util.List;

import at.splendit.simonykees.sample.test.AbstractReflectiveMethodTester;
import at.splendit.simonykees.sample.test.ParameterType;
import at.splendit.simonykees.sample.utilities.NumberUtils;
import at.splendit.simonykees.sample.utilities.StringUtils;

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
		StringUtils stringUtils;
		StringUtils.doesntDoAnything();
		NumberUtils numUtil;
		NumberUtils.explode();
		List<String> list;
		AbstractReflectiveMethodTester tester;
		ParameterType paramType;
		return testString.indexOf("e");
	}
}
