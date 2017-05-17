package at.splendit.simonykees.sample.postRule.standardLogger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"unused", "nls"})
public class TestStandardLoggerRule {

	private static final Logger logger1 = LoggerFactory.getLogger(TestStandardLoggerRule.class);
	private static String logger;
	
	public void replaceSystemOutPrint(String input) {
		logger1.debug(input);
	}
	
	public void replaceSystemOutPrintln(String input) {
		logger1.debug(input);
	}
	
	public void replaceSystemErrPrint(String input) {
		logger1.debug(input);
	}
	
	public void replaceSystemErrPrintln(String input) {
		logger1.debug(input);
	}
	
	public void replacePrintStackTrace(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			logger1.error(e.getMessage(), e);
		}
	}
	
	class InnerClass {
		
		private final Logger logger2 = LoggerFactory.getLogger(InnerClass.class);

		public void dontUseOuterClassLogger(String input) {
			logger2.debug("a log message");
		}
		
		class DoubleNestedInnerClass {
			
			private final Logger logger3 = LoggerFactory.getLogger(DoubleNestedInnerClass.class);

			public void loggerInDoubleNestedClass(String input) {
				logger3.debug(input);
			}
			
			public void useDeepNestedLogger(String input) {
				logger3.debug(input);
			}
		}
		
		public void useCorrectLogger(String input) {
			logger2.debug(input);
		}
	}
	
	public void replaceAfterInnerClass(String input) {
		logger1.debug(input);
	}
}

class TopLevelClass {
	
	private static final Logger logger = LoggerFactory.getLogger(TopLevelClass.class);

	public void replaceSystemOutPrint(String input) {
		logger.debug(input);
	}
	
	public void replaceSystemErrPrintln(String input) {
		logger.debug(input);
	}
	
	public void replacePrintStackTrace(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}

@SuppressWarnings({"unused", "nls"})
enum Days {
	Mon, Tue, Thu, Frii, Saaat, Sun;

	private static final Logger logger1 = LoggerFactory.getLogger(Days.class);
	private static final String logger = "";
	public void loggerInEnumType(String input) {
		logger1.debug(input);
	}
}
