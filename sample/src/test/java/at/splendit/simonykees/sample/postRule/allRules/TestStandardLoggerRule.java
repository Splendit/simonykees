package at.splendit.simonykees.sample.postRule.allRules;

import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unused", "nls" })
public class TestStandardLoggerRule {

	private static final Logger logger1 = LoggerFactory.getLogger(TestStandardLoggerRule.class);
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
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			logger1.error(e.getMessage(), e);
		}
	}

	public void replaceAfterInnerClass(String input) {
		logger1.info(input);
	}

	class InnerClass {

		private final Logger logger2 = LoggerFactory.getLogger(InnerClass.class);
		Consumer<String> p = logger2::info;

		{
			{
				logger2.info("a log message");
			}
		}

		public void dontUseOuterClassLogger(String input) {
			logger2.info("a log message");
		}

		public void useCorrectLogger(String input) {
			logger2.info(input);
		}

		class DoubleNestedInnerClass {

			private final Logger logger3 = LoggerFactory.getLogger(DoubleNestedInnerClass.class);

			public void loggerInDoubleNestedClass(String input) {
				logger3.info(input);
			}

			public void useDeepNestedLogger(String input) {
				logger3.info(input);
			}
		}
	}
}

class TopLevelClass {

	private static final Logger logger = LoggerFactory.getLogger(TopLevelClass.class);

	public void replaceSystemOutPrint(String input) {
		logger.info(input);
	}

	public void replaceSystemErrPrintln(String input) {
		logger.info(input);
	}

	public void replacePrintStackTrace(String input) {
		try {
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}

@SuppressWarnings({ "unused", "nls" })
enum Days {
	Mon, Tue, Thu, Frii, Saaat, Sun;

	private static final Logger logger1 = LoggerFactory.getLogger(Days.class);
	private static final String logger = "";

	public void loggerInEnumType(String input) {
		logger1.info(input);
	}
}

interface OneHavingAnImplementedMethod {

	static final Logger logger = LoggerFactory.getLogger(OneHavingAnImplementedMethod.class);

	default void makeUseOfSystemOut(String input) {
		logger.info(input);
	}
}
