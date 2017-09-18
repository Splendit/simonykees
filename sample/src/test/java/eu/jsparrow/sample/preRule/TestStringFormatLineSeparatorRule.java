package eu.jsparrow.sample.preRule;

import java.util.Locale;

@SuppressWarnings("nls")
public class TestStringFormatLineSeparatorRule {

	public String testStringFormatLineSeparator01() {

		return String.format("\n\n");

	}

	public String testStringFormatLineSeparator02() {

		return String.format(Locale.GERMAN, "\n\n");

	}

	public String testStringFormatLineSeparator03() {

		return String.format("\r\n\r\n");

	}

	public String testStringFormatLineSeparator04() {

		return String.format(Locale.GERMAN, "\r\n\r\n%n");

	}

}
