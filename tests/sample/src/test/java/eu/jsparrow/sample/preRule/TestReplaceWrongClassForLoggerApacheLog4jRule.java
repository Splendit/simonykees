package eu.jsparrow.sample.preRule;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import eu.jsparrow.sample.utilities.HelloWorld;

public class TestReplaceWrongClassForLoggerApacheLog4jRule {
	static final Class<HelloWorld> HRLLO_WORLD_CLASS = HelloWorld.class;
	static final Logger LOGGER = LogManager.getLogger(HelloWorld.class);

	static class ClassWithLoggingMethodInvocations {

		Object message;
		Priority priority;

		void callLoggingMethods() {

			LogManager.getLogger(HelloWorld.class)
				.debug(message);
			LogManager.getLogger(HelloWorld.class)
				.error(message);
			LogManager.getLogger(HelloWorld.class)
				.fatal(message);
			LogManager.getLogger(HelloWorld.class)
				.info(message);
			LogManager.getLogger(HelloWorld.class)
				.log(priority, message);
			LogManager.getLogger(HelloWorld.class)
				.trace(message);
			LogManager.getLogger(HelloWorld.class)
				.warn(message);
		}
	}
}