package eu.jsparrow.sample.preRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SIM-1337
 *
 */
public class TestStandardLoggerExistingNonStaticSlf4jLogger {

	private final Logger logger = LoggerFactory.getLogger(TestStandardLoggerExistingNonStaticSlf4jLogger.class);

	public void testingLogger() {
		logger.info("");
		System.out.print("my val");
	}

	public static void noChangesInStaticMethods() {
		System.out.println("no changes here");
	}
}
