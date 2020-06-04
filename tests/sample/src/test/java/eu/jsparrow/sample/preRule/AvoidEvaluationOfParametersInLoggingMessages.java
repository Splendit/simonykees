package eu.jsparrow.sample.preRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvoidEvaluationOfParametersInLoggingMessages {

	private static final Logger logger = LoggerFactory.getLogger(AvoidEvaluationOfParametersInLoggingMessages.class);

	/**
	 * Testing all the log levels. All statements should be transformed to use a
	 * parameter instead of the '+'
	 * 
	 * @param something
	 */
	public void logSomething(String something) {
		logger.trace("Print " + something);
		logger.debug("Print " + something);
		logger.info("Print " + something);
		logger.warn("Print " + something);
		logger.error("Print " + something);
	}

	/**
	 * Testing all the log levels with an additional {@link Throwable} as
	 * argument. All statements should be transformed to use a parameter instead
	 * of the '+'
	 * 
	 * @param something
	 */
	public void logSomethingWithThrowable(String something, Throwable t, Exception e, RuntimeException r, Error err,
			NullPointerException npe) {
		logger.trace("Print " + something, t);
		logger.debug("Print " + something, e);
		logger.info("Print " + something, r);
		logger.warn("Print " + something, err);
		logger.error("Print " + something, npe);
	}

	/**
	 * Testing all log levels. None of the statements should be transformed,
	 * since they do not start with a String literal.
	 * 
	 * @param something
	 */
	public void logSomething_noChange_wrongOrder(String something) {
		logger.trace(something + " is being printed");
		logger.debug(something + " is being printed");
		logger.info(something + " is being printed");
		logger.warn(something + " is being printed");
		logger.error(something + " is being printed");
	}

	public void logSomething_change_addParameters() {
		logger.info("A " + 1 + " B " + 2 + " C " + 3 + " D " + 4);
		logger.info("A " + 1 + " B " + 2 + " C " + 3 + " D " + 4 + " E " + new Exception("5").getMessage());
		logger.info("A " + 1 + " B " + 2 + " C " + 3 + " D " + 4 + " E ", new Exception("5"));
	}

	/**
	 * None of the statements should be transformed, since they already contain
	 * a parameter.
	 * 
	 * @param something
	 */
	public void logSomething_noChange_containsArgument(String b, String c, Exception e) {
		logger.info("A " + 1 + " B {}" + 2);
		logger.info("A " + 1 + " B {}", 2);
		logger.info("A {} " + c, b); // A {b} {c}
		logger.error("A {} " + c, b, e); // A {b} {c} {e}
		logger.info("A {} " + "C" + " {}", "B", "D"); // A B C D
		logger.info("A " + "B " + c + " {}", "D"); // A B {c} D
		logger.info("A {} " + c + " {}", "B", "D", e); // A B {c} D {e}
		logger.info("A " + "B " + c + " {}", "D", e); // A B {c} D {e}
		logger.info("A " + 1 + " B {}", 2); // A 1 B 2
		logger.info("A " + 1 + " B {}" + " C" + " {} {}", 2, 3, "D " + 4); // A
																			// 1
																			// B
																			// 2
																			// C
																			// 3
																			// D
																			// 4
	}

	public void test(String something, Exception e) {
		// pre: 	logger.info("A " + 1 + " B " + 2);
		// target:  logger.info("A {} B {}", 1, 2);
		logger.info("A " + 1 + " B " + 2);

		logger.info("A {}{}{}", 1, " B {}", 2);

		logger.info("A {}{}{}", 1, " B {}", 2, "C");

		logger.info("A {}" + 1 + " B " + 2);

		logger.info("A {}{}{}{}", 1, " B ", 2);

		logger.info("A {}{}{}{}", 1, " B ", 2, " C");
		
		logger.info("A " + 1 + " B " + 2 + " C " + 3 + " D " + 4);
		
		logger.info("A " + "X" + " B " + 2 + " C " + 3 + " D " + 4);
		
		logger.info("A " + "B " + 1);
	}
}
