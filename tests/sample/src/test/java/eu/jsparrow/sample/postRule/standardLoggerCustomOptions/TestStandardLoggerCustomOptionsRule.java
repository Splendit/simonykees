package eu.jsparrow.sample.postRule.standardLoggerCustomOptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStandardLoggerCustomOptionsRule {
	
	private static final Logger logger = LoggerFactory.getLogger(TestStandardLoggerCustomOptionsRule.class);

	public void replaceSystemOutPrint(String input) {
		System.out.print(input);
	}
	
	public void replaceSystemOutPrintln(String input) {
		System.out.println(input);
	}
	
	public void replaceSystemErrPrint(String input) {
		logger.debug(input);
	}
	
	public void replaceSystemErrPrintln(String input) {
		logger.debug(input);
	}
	
	public void replacePrintStackTrace(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}
}
