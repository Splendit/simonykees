package at.splendit.simonykees.sample.postRule.allRules;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStandardLoggerCustomOptionsRule {

	private static final Logger logger = LoggerFactory.getLogger(TestStandardLoggerCustomOptionsRule.class);

	public void replaceSystemOutPrint(String input) {
		logger.info(input);
	}

	public void replaceSystemOutPrintln(String input) {
		logger.info(input);
	}

	public void replaceSystemErrPrint(String input) {
		logger.error(input);
	}

	public void replaceSystemErrPrintln(String input) {
		logger.error(input);
	}

	public void replacePrintStackTrace(String input) {
		try {
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
