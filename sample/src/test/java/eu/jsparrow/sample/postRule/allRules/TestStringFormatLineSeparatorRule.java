package eu.jsparrow.sample.postRule.allRules;

import java.util.Locale;

@SuppressWarnings("nls")
public class TestStringFormatLineSeparatorRule {

	public String testStringFormatLineSeparator01() {

		return String.format("%n%n");

	}

	public String testStringFormatLineSeparator02() {

		return String.format(Locale.GERMAN, "%n%n");

	}

	public String testStringFormatLineSeparator03() {

		return String.format("%n%n");

	}

	public String testStringFormatLineSeparator04() {

		return String.format(Locale.GERMAN, "%n%n%n");

	}

}
