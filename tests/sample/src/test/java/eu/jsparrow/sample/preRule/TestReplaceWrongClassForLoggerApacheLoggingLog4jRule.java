package eu.jsparrow.sample.preRule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerApacheLoggingLog4jRule {
	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = LogManager.getLogger(HelloWorld.class);
}