package eu.jsparrow.sample.postRule.allRules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerApacheLoggingLog4jRule {
	private TestReplaceWrongClassForLoggerApacheLoggingLog4jRule() {
		throw new IllegalStateException("Utility class");
	}

	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = LogManager.getLogger(TestReplaceWrongClassForLoggerApacheLoggingLog4jRule.class);
}