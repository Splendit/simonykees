package eu.jsparrow.sample.postRule.loggingWithForeignClass;

import java.util.logging.Logger;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerJavaLoggingRule {
	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = Logger.getLogger(TestReplaceWrongClassForLoggerJavaLoggingRule.class.getName());
}