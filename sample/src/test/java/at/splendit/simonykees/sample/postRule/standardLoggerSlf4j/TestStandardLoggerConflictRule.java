package at.splendit.simonykees.sample.postRule.standardLoggerSlf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class TestStandardLoggerConflictRule {

	private static final Logger logger = LoggerFactory.getLogger(TestStandardLoggerConflictRule.class);

	public void dontReplace(String input) {
		logger.info(input);
	}
	
	class Logger1 {
		
		private final Logger logger1 = LoggerFactory.getLogger(Logger1.class);

		public void debut(String input) {
			logger1.info("input");
		}
		
		public void warn(String input) {
			logger1.info("input");
		}
		
		public void error(String input) {
			logger1.info("input");
		}
		
	}
}
