package eu.jsparrow.sample.postRule.allRules;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TestReplaceLoggingWithForeignClassApacheLog4jRule {
	private TestReplaceLoggingWithForeignClassApacheLog4jRule() {
		throw new IllegalStateException("Utility class");
	}

	static final Logger LOGGER = LogManager.getLogger(TestReplaceLoggingWithForeignClassApacheLog4jRule.class);
}