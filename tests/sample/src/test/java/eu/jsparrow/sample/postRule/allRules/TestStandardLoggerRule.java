package eu.jsparrow.sample.postRule.allRules;

import java.util.Locale;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unused", "nls" })
public class TestStandardLoggerRule {

	private static final Logger logger1 = LoggerFactory.getLogger(TestStandardLoggerRule.class);

	private static String logger;

	private final String e = "I am not an exception";

	public void printingExceptionsAndPrimitives(String input) {
		final Exception e = new Exception("Made up exception...");
		logger1.error(String.valueOf(new Exception()));
		System.out.println();
		logger1.info(String.valueOf(6));
		logger1.info(String.valueOf(new char[] { 'c', 'd', 'e' }));
		logger1.error(String.valueOf(new Exception("adsfads")));
		logger1.info("adsfads");
		logger1.error(String.valueOf(e), e);

		logger1.info("");
		logger1.info("%d - %d", 5, 6);
		logger1.info(String.format(Locale.GERMANY, "%d - %d", 5, 6));
		logger1.info(String.format(Locale.GERMANY, "%d - %d"));
	}

	public void replaceSystemOutPrintingException(String input) {
		try {
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			logger1.error(e.getMessage(), e);
		}
	}

	public void replaceSystemErrPrintingException(String input) {
		try {
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			logger1.error(e.getMessage(), e);
		}
	}

	public void replaceSystemOutPrintFormatException(String input) {
		try {
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			logger1.error(String.format("%d : " + e.getMessage(), 1), e);
			logger1.info("%d : val %d : ", 1, 2);
			logger1.info(String.format(Locale.FRANCE, "%d : val %d : ", 1, 2));
		}
	}

	public void insertLoggingStatementInEmptycatchClasuse(String input) {
		try {
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			logger1.error(e.getMessage(), e);

		}
	}

	public void insertMissingLoggingStatementInCatchClasuse(String input) {
		try {
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			/*
			 * The catch clause is not empty, but the exception is not logged.
			 */

			logger1.error(e.getMessage(), e);
			logger1.info("Nothing to show");
		}
	}

	public void distinguishLoggedExceptionFromField(String input) {
		try {
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			/*
			 * The catch clause is not empty, but the exception is not logged.
			 */

			logger1.error(e.getMessage(), e);
			logger1.info(this.e);
		}
	}

	public void nestedCatchClauses(String input) {
		try {
			StringUtils.substring(input, 5);
		} catch (Exception e) {
			try {
				StringUtils.substring(input, 5);
			} catch (Exception e1) {
				logger1.error(e1.getMessage(), e1);
				logger1.error(e.getMessage(), e);
			}
		}
	}

	public void replaceSystemOutPrint(String input) {
		logger1.info(input);
	}

	public void replaceSystemOutPrintln(String input) {
		logger1.info(input);
	}

	public void replaceSystemOutPrintf(String input) {
		logger1.info("%d : " + input, 1);
		logger1.info(String.format(Locale.GERMANY, "%d : " + input, 1));
	}

	public void replaceSystemErrPrint(String input) {
		logger1.error(input);
	}

	public void replaceSystemErrPrintln(String input) {
		logger1.error(input);
	}

	public void replaceSystemErrPrintf(String input) {
		logger1.error("%d : " + input, 1);
		logger1.error(String.format(Locale.GERMANY, "%d : " + input, 1));
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

@SuppressWarnings({ "unused", "nls" })
enum Days {
	Mon,
	Tue,
	Thu,
	Frii,
	Saaat,
	Sun;

	private static final Logger logger1 = LoggerFactory.getLogger(Days.class);
	private static final String logger = "";

	public void loggerInEnumType(String input) {
		logger1.info(input);
	}
}

interface OneHavingAnImplementedMethod {

	Logger logger = LoggerFactory.getLogger(OneHavingAnImplementedMethod.class);

	default void makeUseOfSystemOut(String input) {
		logger.info(input);
	}
}
