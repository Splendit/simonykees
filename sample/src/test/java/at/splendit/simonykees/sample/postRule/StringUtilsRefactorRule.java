package at.splendit.simonykees.sample.postRule;

import java.util.Arrays;
import java.util.Locale;

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
		return testString.split(",");
	}

	public String testSplitCornerCase(String testString) {
		testString = complexSplit(testString, "?", 0);
		testString = complexSplit(testString, "|", 0);
		testString = complexSplit(testString, ",", 0);
		testString = complexSplit(testString, "a", 0);

		return testString;
	}
	
	public String testSplitCornerCaseLimit(String testString) {
		int limit = 3;
		
		testString = complexSplit(testString, "?", limit);
		testString = complexSplit(testString, "|", limit);
		testString = complexSplit(testString, ",", limit);
		testString = complexSplit(testString, "a", limit);
		
		return testString;
	}

	private String complexSplit(String input, String splitSign, int limit) {
		if (StringUtils.contains(input, splitSign)) {
			if (StringUtils.equals("?", splitSign)) {
				/*
				 * We need to escape the "?" because otherwise there is the
				 * following exception: java.util.regex.PatternSyntaxException:
				 * Dangling meta character '?' near index 0
				 */
				splitSign = StringUtils.replace(splitSign, "?", "\\?");
			}
			if (StringUtils.equals("|", splitSign)) {
				/*
				 * We need to escape the "|" because otherwise an empty String
				 * is taken as split sign.
				 */
				splitSign = StringUtils.replace(splitSign, "|", "\\|");
			}
			return limit == 0 ? Arrays.toString(input.split(splitSign))
					: Arrays.toString(input.split(splitSign, limit));
		} else {
			return input;
		}
	}

	public String testReplaceCornerCaseCharSequence(String testString) {
		CharSequence c1 = new StringBuilder("a");
		CharSequence c2 = new StringBuilder("b");

		// FIXME see SIM-85 
		return StringUtils.replace(testString, String.valueOf(c1), String.valueOf(c2));
	}

	public String testReplaceCornerCaseChar(String testString) {
		char c1 = 'a';
		char c2 = 'b';

		// FIXME see SIM-85
		return StringUtils.replace(testString, String.valueOf(c1), String.valueOf(c2));
	}

	public boolean testEqualsCornerCase(String testString) {
		Object o = "s";

		return StringUtils.equals(testString, String.valueOf(o)); // FIXME see SIM-86
	}

	public boolean testStartsWithCornerCase(String testString) {
		String prefix = "a";
		int toffset = 1;

		return testString.startsWith(prefix, toffset);
	}
	
	public String testUpperCaseCornerCase(String testString) {
		Locale l = Locale.GERMAN;
		
		return StringUtils.upperCase(testString, l);
	}
	
	public String testLowerCaseCornerCase(String testString) {
		Locale l = Locale.GERMAN;
		
		return StringUtils.lowerCase(testString, l);
	}

}
