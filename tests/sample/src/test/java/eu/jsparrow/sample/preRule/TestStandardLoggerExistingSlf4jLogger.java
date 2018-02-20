package eu.jsparrow.sample.preRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class TestStandardLoggerExistingSlf4jLogger {

	private static final Logger logger = LoggerFactory.getLogger(TestStandardLoggerExistingSlf4jLogger.class);

	public void testingLogger() {
		logger.info("");
		System.out.print("my val");
	}
}