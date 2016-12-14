package at.splendit.simonykees.sample.preRule;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("nls")
public class TestToStringOnStringRule {

	public String testToStringOnStringLiteral() {

		return "anStringLiteral".toString();

	}

	public String testToStringOnStringVariable() {
		String s = "theStringS";

		return s.toString();

	}

	public String testToStringOnStringFunctionThatReturnsString() {

		return StringUtils.abbreviate("makeMeShorter", 4).toString();

	}

	public String testToStringOnParenthesizeString(String s) {

		return (s).toString();

	}

	public String testToStringOnParenthesizePlusString(String s) {

		return (s + "abc").toString();

	}
}
