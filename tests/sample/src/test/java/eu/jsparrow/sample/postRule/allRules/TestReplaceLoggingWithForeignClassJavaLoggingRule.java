package eu.jsparrow.sample.postRule.allRules;

import java.util.logging.Logger;

public class TestReplaceLoggingWithForeignClassJavaLoggingRule {
	private TestReplaceLoggingWithForeignClassJavaLoggingRule() {
		throw new IllegalStateException("Utility class");
	}

	static final Logger LOGGER = Logger.getLogger(TestReplaceLoggingWithForeignClassJavaLoggingRule.class.getName());
}