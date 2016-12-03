package at.splendit.simonykees.sample.postRule.allRules;

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
}
