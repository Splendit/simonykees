package eu.jsparrow.sample.postRule.allRules;

import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Martin Huter
 *
 */

@SuppressWarnings("nls")
public class StringUtilsRefactorRule {

	public boolean testEmpty(String testString) {
		// save me
		return /* test */ StringUtils.isEmpty(testString) /* trailing comment */;
	}

	public String testTrim(String testString) {
		final String val = /* test */StringUtils.trim(testString);
		return StringUtils.trim(testString);
	}

	public boolean testEquals(String testString) {
		final String sometimesExpectedString = testString.replaceAll("a", "b");

		return testString.equals(sometimesExpectedString);
	}

	public boolean testEqualsIgnoreCase(String testString) {
		final String sometimesExpectedString = testString.replaceAll("a", "b");

		return StringUtils.equalsIgnoreCase(testString, sometimesExpectedString);
	}

	public boolean testEndsWith(String testString) {
		final String sometimesExpectedString = "With";

		return StringUtils.endsWith(testString, sometimesExpectedString);
	}

	public boolean testStartWith(String testString) {
		final String sometimesExpectedString = "start";
		testString.startsWith(sometimesExpectedString, 1);
		return StringUtils.startsWith(testString, sometimesExpectedString);
	}

	public int testIndexOf(String testString) {
		StringUtils.indexOf(testString, 0);
		StringUtils.indexOf(testString, 4, 0);
		StringUtils.indexOf(testString, "e", 1);
		return StringUtils.indexOf(testString, "e");
	}

	public boolean testContains(String testString) {
		final String sometimesExpectedString = "tain";

		return StringUtils.contains(testString, sometimesExpectedString);
	}

	public String testReplace(String testString) {
		return testString.replace("M", "m");
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
		final int limit = 3;

		testString = complexSplit(testString, "?", limit);
		testString = complexSplit(testString, "|", limit);
		testString = complexSplit(testString, ",", limit);
		testString = complexSplit(testString, "a", limit);

		return testString;
	}

	private String complexSplit(String input, String splitSign, int limit) {
		if (!StringUtils.contains(input, splitSign)) {
			return input;
		}
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
			 * We need to escape the "|" because otherwise an empty String is
			 * taken as split sign.
			 */
			splitSign = splitSign.replace("|", "\\|");
		}
		return limit == 0 ? Arrays.toString(input.split(splitSign)) : Arrays.toString(input.split(splitSign, limit));
	}

	public String testReplaceCornerCaseCharSequence(String testString) {
		final CharSequence c1 = new StringBuilder("a");
		final CharSequence c2 = new StringBuilder("b");

		// FIXME see SIM-85
		return testString.replace(String.valueOf(c1), String.valueOf(c2));
	}

	public String testReplaceCornerCaseChar(String testString) {
		final char c1 = 'a';
		final char c2 = 'b';

		// FIXME see SIM-85
		return testString.replace(String.valueOf(c1), String.valueOf(c2));
	}

	public boolean testEqualsCornerCase(String testString) {
		final Object o = "s";

		return testString.equals(String.valueOf(o)); // FIXME see SIM-86
	}

	public boolean testStartsWithCornerCase(String testString) {
		final String prefix = "a";
		final int toffset = 1;

		return testString.startsWith(prefix, toffset);
	}

	public String testUpperCaseCornerCase(String testString) {
		final Locale l = Locale.GERMAN;
		StringUtils.upperCase(testString);
		return StringUtils.upperCase(testString, l);
	}

	public String testLowerCaseCornerCase(String testString) {
		final Locale l = Locale.GERMAN;
		StringUtils.lowerCase(testString);
		return StringUtils.lowerCase(testString, l);
	}

	public String testSubstring(String testString) {
		StringUtils.substring(testString, 0);
		return StringUtils.substring(testString, 0, 0);
	}

	// Reproduces SIM-319
	public String testNestedApplication(String testString) {
		String url = "testString/generate-skus";
		url = StringUtils.substring(url, 0, StringUtils.indexOf(url, "/generate-skus"));
		return testString;
	}

}
