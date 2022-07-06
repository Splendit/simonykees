package eu.jsparrow.sample.postRule.allRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestReplaceLoggingWithForeignClassSlf4jRule {
	private TestReplaceLoggingWithForeignClassSlf4jRule() {
		throw new IllegalStateException("Utility class");
	}

	static final Logger LOGGER = LoggerFactory.getLogger(TestReplaceLoggingWithForeignClassSlf4jRule.class);
}