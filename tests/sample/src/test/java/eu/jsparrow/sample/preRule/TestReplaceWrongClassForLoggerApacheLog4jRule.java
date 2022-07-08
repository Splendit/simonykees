package eu.jsparrow.sample.preRule;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerApacheLog4jRule {
	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = LogManager.getLogger(HelloWorld.class);
}