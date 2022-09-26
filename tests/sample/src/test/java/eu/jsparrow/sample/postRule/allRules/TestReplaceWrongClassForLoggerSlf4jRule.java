package eu.jsparrow.sample.postRule.allRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerSlf4jRule {
	private TestReplaceWrongClassForLoggerSlf4jRule() {
		throw new IllegalStateException("Utility class");
	}

	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = LoggerFactory.getLogger(TestReplaceWrongClassForLoggerSlf4jRule.class);
}