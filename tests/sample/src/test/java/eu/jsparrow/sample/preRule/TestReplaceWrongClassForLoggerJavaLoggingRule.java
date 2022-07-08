package eu.jsparrow.sample.preRule;

import java.util.logging.Logger;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerJavaLoggingRule {
	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = Logger.getLogger(HelloWorld.class.getName());
}