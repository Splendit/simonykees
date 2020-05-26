package eu.jsparrow.sample.preRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unused", "nls"})
public class AvoidEvaluationOfParametersInLoggingMessages {

	private static final Logger logger = LoggerFactory.getLogger(AvoidEvaluationOfParametersInLoggingMessages.class);
	
	public void logSomething(String something) {
		logger.trace("Print " + something);
		logger.debug("Print " + something);
		logger.info("Print " + something);
		logger.warn("Print " + something);
		logger.error("Print " + something);
	}
	
}
