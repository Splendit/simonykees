package eu.jsparrow.sample.postRule.allRules;

import java.util.logging.Logger;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerJavaLoggingRule {
	private TestReplaceWrongClassForLoggerJavaLoggingRule() {
		throw new IllegalStateException("Utility class");
	}

	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = Logger.getLogger(TestReplaceWrongClassForLoggerJavaLoggingRule.class.getName());
}