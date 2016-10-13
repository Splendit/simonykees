package at.splendit.simonykees.sample.preRule;

import java.util.Arrays;

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
public class StringUtilsRefactorRule {

	public boolean testEmpty(String testString) {
		return testString.isEmpty();
	}

	public String testTrim(String testString) {
		return testString.trim();
	}

	public boolean testEquals(String testString) {
		String sometimesExpectedString = testString.replaceAll("a", "b");

		return testString.equals(sometimesExpectedString);
	}

	public boolean testEqualsIgnoreCase(String testString) {
		String sometimesExpectedString = testString.replaceAll("a", "b");

		return testString.equalsIgnoreCase(sometimesExpectedString);
	}

	public boolean testEndsWith(String testString) {
		String sometimesExpectedString = "With";

		return testString.endsWith(sometimesExpectedString);
	}

	public boolean testStartWith(String testString) {
		String sometimesExpectedString = "start";

		return testString.startsWith(sometimesExpectedString);
	}

	public int testIndexOf(String testString) {
		return testString.indexOf("e");
	}

	public boolean testContains(String testString) {
		String sometimesExpectedString = "tain";

		return testString.contains(sometimesExpectedString);
	}

	public String testReplace(String testString) {
		return testString.replace("M", "m");
	}

	public String testLowerCase(String testString) {
		return testString.toLowerCase();
	}

	public String testUpperCase(String testString) {
		return testString.toUpperCase();
	}

	public String[] testSplit(String testString) {
		return testString.split(",");
	}

	public String complexSplit(String testString) {
		testString = split(testString, "?");
		testString = split(testString, "|");
		testString = split(testString, ",");
		testString = split(testString, "a");

		return testString;
	}

	private String split(String input, String splitSign) {
		if (input.contains(splitSign)) {
			if ("?".equals(splitSign)) {
				/*
				 * We need to escape the "?" because otherwise there is the
				 * following exception: java.util.regex.PatternSyntaxException:
				 * Dangling meta character '?' near index 0
				 */
				splitSign = splitSign.replace("?", "\\?");
			}
			if ("|".equals(splitSign)) {
				/*
				 * We need to escape the "|" because otherwise an empty String
				 * is taken as split sign.
				 */
				splitSign = splitSign.replace("|", "\\|");
			}
			return Arrays.toString(input.split(splitSign));
		} else
			return input;
	}
	
	public String testReplaceCornerCase(String testString) {
		CharSequence c1 = new StringBuilder("a");
		CharSequence c2 = new StringBuilder("b");
		
		return testString.replace(c1.toString(), c2.toString()); // FIXME see SIM-85
	}
	
	public boolean testEqualsCornerCase(String testString) {
		Object o = "s";
		
		return testString.equals(o.toString()); // FIXME see SIM-86
	}

}
