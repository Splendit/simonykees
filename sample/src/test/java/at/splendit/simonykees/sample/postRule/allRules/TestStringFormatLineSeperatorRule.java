package at.splendit.simonykees.sample.postRule.allRules;

import java.util.Locale;

@SuppressWarnings("nls")
public class TestStringFormatLineSeperatorRule {

	public String testStringFormatLineSeperator01() {

		return String.format("%n%n");

	}

	public String testStringFormatLineSeperator02() {

		return String.format(Locale.GERMAN, "%n%n");

	}

	public String testStringFormatLineSeperator03() {

		return String.format("%n%n");

	}

	public String testStringFormatLineSeperator04() {

		return String.format(Locale.GERMAN, "%n%n%n");

	}

}
