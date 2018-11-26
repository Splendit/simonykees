package eu.jsparrow.sample.postRule.allRules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.sample.postRule.standardLoggerSlf4j.TestStandardLoggerExistingSlf4jLogger;

public class TestStandardLoggerExistingNonStaticSlf4jLogger {
	private final Logger logger = LoggerFactory.getLogger(TestStandardLoggerExistingSlf4jLogger.class);

	public void testingLogger() {
		logger.info("");
		logger.info("my val");
	}
	
	public static void noChangesInStaticMethods() {
		System.out.println("no changes here");
	}
}
