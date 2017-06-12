package at.splendit.simonykees.sample.postRule.standardLoggerLog4j;

import java.util.function.Consumer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@SuppressWarnings({"unused", "nls"})
public class TestStandardLoggerRule {

	private static final Logger logger1 = LogManager.getLogger(TestStandardLoggerRule.class);
	private static String logger;
	
	public void replaceSystemOutPrint(String input) {
		logger1.info(input);
	}
	
	public void replaceSystemOutPrintln(String input) {
		logger1.info(input);
	}
	
	public void replaceSystemErrPrint(String input) {
		logger1.info(input);
	}
	
	public void replaceSystemErrPrintln(String input) {
		logger1.info(input);
	}
	
	public void replacePrintStackTrace(String input) {
		try {
			input.substring(5);
		} catch (Exception e) {
			logger1.error(e.getMessage(), e);
		}
	}
	
	class InnerClass {
		
		private final Logger logger2 = LogManager.getLogger(InnerClass.class);

		{{
			logger2.info("a log message");
		}}
		
		Consumer<String> p = (String input) -> {
			logger2.info(input);
		};
		
		public void dontUseOuterClassLogger(String input) {
			logger2.info("a log message");
		}
		
		class DoubleNestedInnerClass {
			
			private final Logger logger3 = LogManager.getLogger(DoubleNestedInnerClass.class);

			public void loggerInDoubleNestedClass(String input) {
				logger3.info(input);
			}
			
			public void useDeepNestedLogger(String input) {
				logger3.info(input);
			}
		}
		
		public void useCorrectLogger(String input) {
			logger2.info(input);
		}
	}
	
	public void replaceAfterInnerClass(String input) {
		logger1.info(input);
	}
}

class TopLevelClass {
	
	private static final Logger logger = LogManager.getLogger(TopLevelClass.class);

	public void replaceSystemOutPrint(String input) {
		logger.info(input);
	}
	
	public void replaceSystemErrPrintln(String input) {
		logger.info(input);
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

	private static final Logger logger1 = LogManager.getLogger(Days.class);
	private static final String logger = "";
	public void loggerInEnumType(String input) {
		logger1.info(input);
	}
}


interface OneHavingAnImplementedMethod {
	
	static final Logger logger = LogManager.getLogger(OneHavingAnImplementedMethod.class);

	default void makeUseOfSystemOut(String input) {
		logger.info(input);
	}
}
