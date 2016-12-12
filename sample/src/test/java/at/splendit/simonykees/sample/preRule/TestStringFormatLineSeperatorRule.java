package at.splendit.simonykees.sample.preRule;

import java.util.Locale;

@SuppressWarnings("nls")
public class TestStringFormatLineSeperatorRule {

	public String testStringFormatLineSeperator01() {

		return String.format("\n\n");

	}

	public String testStringFormatLineSeperator02() {

		return String.format(Locale.GERMAN, "\n\n");

	}

	public String testStringFormatLineSeperator03() {

		return String.format("\r\n\r\n");

	}

	public String testStringFormatLineSeperator04() {

		return String.format(Locale.GERMAN, "\r\n\r\n%n");

	}

}
