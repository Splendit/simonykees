package eu.jsparrow.sample.postRule.standardLoggerSlf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SIM-1337
 *
 */
public class TestStandardLoggerExistingNoModifiersSlf4jLogger {

	Logger logger = LoggerFactory.getLogger(TestStandardLoggerExistingNoModifiersSlf4jLogger.class);

	public void testingLogger() {
		logger.info("");
		logger.info("my val");
	}

	public static void noChangesInStaticMethods() {
		System.out.println("no changes here");
	}
}
