package eu.jsparrow.sample.postRule.allRules;

/**
 * Having an inner class named StringUtils.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
@SuppressWarnings("nls")
public class StringUtilsInnerClassCornerCaseRule {

	public int testIndexOf(String testString) {
		return org.apache.commons.lang3.StringUtils.indexOf(testString, "e");
	}

	class StringUtils {
		public int indexOf(String s1, String s2) {
			// do nothing
			return 0;
		}
	}
}
