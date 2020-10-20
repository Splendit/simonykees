package eu.jsparrow.standalone;

import org.apache.commons.lang3.StringUtils;

public enum StandaloneMode {
	TEST,
	REFACTOR,
	DEMO,
	LIST_RULES,
	LIST_RULES_SHORT,
	LICENSE_INFO,
	NONE;

	@SuppressWarnings("nls")
	public static StandaloneMode fromString(String value) {
		if (value == null) {
			return NONE;
		}
		String upperCaseValue = StringUtils.upperCase(value);

		switch (upperCaseValue) {
		case "TEST":
			return TEST;
		case "REFACTOR":
			return REFACTOR;
		case "DEMO":
			return DEMO;
		case "LIST_RULES":
			return LIST_RULES;
		case "LIST_RULES_SHORT":
			return LIST_RULES_SHORT;
		case "LICENSE_INFO":
			return LICENSE_INFO;
		default:
			return NONE;

		}
	}
}