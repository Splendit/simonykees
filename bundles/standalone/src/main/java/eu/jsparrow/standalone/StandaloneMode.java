package eu.jsparrow.standalone;

public enum StandaloneMode {
	TEST,
	REFACTOR,
	LIST_RULES,
	LIST_RULES_SHORT,
	LICENSE_INFO,
	NONE;

	@SuppressWarnings("nls")
	public static StandaloneMode fromString(String value) {
		if (value == null) {
			return NONE;
		}
		String upperCaseValue = value.toUpperCase();

		switch (upperCaseValue) {
		case "TEST":
			return TEST;
		case "REFACTOR":
			return REFACTOR;
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