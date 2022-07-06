package eu.jsparrow.sample.postRule.allRules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestReplaceLoggingWithForeignClassApacheLoggingLog4jRule {
	private TestReplaceLoggingWithForeignClassApacheLoggingLog4jRule() {
		throw new IllegalStateException("Utility class");
	}

	static final Logger LOGGER = LogManager.getLogger(TestReplaceLoggingWithForeignClassApacheLoggingLog4jRule.class);
}