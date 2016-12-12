package at.splendit.simonykees.sample.postRule.toString;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("nls")
public class TestToStringOnStringRule {

	public String testToStringOnStringLiteral() {

		return "anStringLiteral";

	}

	public String testToStringOnStringVariable() {
		String s = "theStringS";

		return s;

	}

	public String testToStringOnStringFunctionThatReturnsString() {

		return StringUtils.abbreviate("makeMeShorter", 4);

	}

	public String testToStringOnParenthesizeString(String s) {

		return s;

	}

	public String testToStringOnParenthesizePlusString(String s) {

		return s + "abc";

	}
}
