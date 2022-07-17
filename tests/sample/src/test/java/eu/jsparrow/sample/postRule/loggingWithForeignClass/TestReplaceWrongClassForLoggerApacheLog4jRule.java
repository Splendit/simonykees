package eu.jsparrow.sample.postRule.loggingWithForeignClass;

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

			LogManager.getLogger(ClassWithLoggingMethodInvocations.class)
				.debug(message);
			LogManager.getLogger(ClassWithLoggingMethodInvocations.class)
				.error(message);
			LogManager.getLogger(ClassWithLoggingMethodInvocations.class)
				.fatal(message);
			LogManager.getLogger(ClassWithLoggingMethodInvocations.class)
				.info(message);
			LogManager.getLogger(ClassWithLoggingMethodInvocations.class)
				.log(priority, message);
			LogManager.getLogger(ClassWithLoggingMethodInvocations.class)
				.trace(message);
			LogManager.getLogger(ClassWithLoggingMethodInvocations.class)
				.warn(message);
		}
	}
}