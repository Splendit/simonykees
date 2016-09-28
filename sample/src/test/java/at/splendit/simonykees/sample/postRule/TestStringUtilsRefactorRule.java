package at.splendit.simonykees.sample.postRule;

import org.apache.commons.lang3.StringUtils;

/**
 * This test is a manual test to provide tests for the StringUtils replacement
 * 
 * 
 * How to Test: Do unit Test -> All tests should pass. Apply the [Usage] on the
 * file. Test the file again -> All tests should still pass and all methods of
 * Strings should be replaced by the corresponding StrinUtils implementation.
 * 
 * Usage: [Right Click in Editor] -> [Simoneykees/SelectRuleWizardHandler] ->
 * [StringUtils auswählen] -> Finish This triggers the Event.
 * 
 * Event: All operations on a String should be replaced by the corresponding
 * StringUtils method.
 * 
 * @author mgh
 *
 */

@SuppressWarnings("nls")
public class TestStringUtilsRefactorRule {

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
