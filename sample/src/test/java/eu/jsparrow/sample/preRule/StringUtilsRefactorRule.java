package eu.jsparrow.sample.preRule;

import java.util.Arrays;
import java.util.Locale;

/**
 * @author Martin Huter
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
			return limit == 0 ? Arrays.toString(input.split(splitSign))
					: Arrays.toString(input.split(splitSign, limit));
		} else
			return input;
	}

	public String testReplaceCornerCaseCharSequence(String testString) {
		CharSequence c1 = new StringBuilder("a");
		CharSequence c2 = new StringBuilder("b");

		// FIXME see SIM-85 
		return testString.replace(String.valueOf(c1), String.valueOf(c2));
	}

	public String testReplaceCornerCaseChar(String testString) {
		char c1 = 'a';
		char c2 = 'b';

		// FIXME see SIM-85
		return testString.replace(String.valueOf(c1), String.valueOf(c2));
	}

	public boolean testEqualsCornerCase(String testString) {
		Object o = "s";

		return testString.equals(String.valueOf(o)); // FIXME see SIM-86
	}

	public boolean testStartsWithCornerCase(String testString) {
		String prefix = "a";
		int toffset = 1;

		return testString.startsWith(prefix, toffset);
	}
	
	public String testUpperCaseCornerCase(String testString) {
		Locale l = Locale.GERMAN;
		
		return testString.toUpperCase(l);
	}
	
	public String testLowerCaseCornerCase(String testString) {
		Locale l = Locale.GERMAN;
		
		return testString.toLowerCase(l);
	}

}
