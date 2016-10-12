package at.splendit.simonykees.sample.postRule;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Martin Huter
 *
 */

@SuppressWarnings("nls")
public class StringUtilsRefactorRule {

	public boolean testEmpty(String testString) {
		return StringUtils.isEmpty(testString);
	}

	public String testTrim(String testString) {
		return StringUtils.trim(testString);
	}

	public boolean testEquals(String testString) {
		String sometimesExpectedString = testString.replaceAll("a", "b");
		
		return StringUtils.equals(testString, sometimesExpectedString);
	}

	public boolean testEqualsIgnoreCase(String testString) {
		String sometimesExpectedString = testString.replaceAll("a", "b");
		
		return StringUtils.equalsIgnoreCase(testString, sometimesExpectedString);
	}

	public boolean testEndsWith(String testString) {
		String sometimesExpectedString = "With";

		return StringUtils.endsWith(testString, sometimesExpectedString);
	}

	public boolean testStartWith(String testString) {
		String sometimesExpectedString = "start";

		return StringUtils.startsWith(testString, sometimesExpectedString);
	}

	public int testIndexOf(String testString) {
		return StringUtils.indexOf(testString, "e");
	}

	public boolean testContains(String testString) {
		String sometimesExpectedString = "tain";

		return StringUtils.contains(testString, sometimesExpectedString);
	}

	public String testReplace(String testString) {
		return StringUtils.replace(testString, "M", "m");
	}

	public String testLowerCase(String testString) {
		return StringUtils.lowerCase(testString);
	}

	public String testUpperCase(String testString) {
		return StringUtils.upperCase(testString);
	}

	public String[] testSplit(String testString) {
		return StringUtils.split(testString, ",");
	}
	
}
