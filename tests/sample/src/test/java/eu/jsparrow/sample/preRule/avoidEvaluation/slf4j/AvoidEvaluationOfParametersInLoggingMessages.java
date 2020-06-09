package eu.jsparrow.sample.preRule.avoidEvaluation.slf4j;

import java.math.BigDecimal;
import java.time.Instant;

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
	public void visit_allLogLevels_shouldTransform(String something) {
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
	 * @param t
	 * @param e
	 * @param r
	 * @param err
	 * @param npe
	 */
	public void visit_throwableParameter_shouldTransform(String something, Throwable t, Exception e, RuntimeException r,
			Error err,
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
	public void visit_wrongOrderOfParameters_shouldNotTransform(String something, Exception e) {
		logger.trace(something + " is being printed");
		logger.debug(something + " is being printed");
		logger.info(something + " is being printed");
		logger.warn(something + " is being printed");
		logger.error(something + " is being printed", e);
	}

	public void visit_combinableStringLiterals_shouldTransform() {
		logger.info("A " + 1 + " B " + 2);
		logger.info("A " + 1 + " B " + 2 + " C " + 3 + " D " + 4);
		logger.info("A " + 1 + " B " + 2 + " C " + 3 + " D " + 4 + " E " + new Exception("5").getMessage());
		logger.info("A " + 1 + " B " + 2 + " C " + 3 + " D " + 4 + " E", new Exception("5"));
	}

	public void visit_variousTypes_shouldTransform(String s, int i, BigDecimal bd, char c, boolean b) {
		logger.info("s: " + s + " i: " + i + " bd: " + bd + " c: " + c + " b: " + b);
		logger.info("s: " + "s" + " i: " + 1);
		logger.info("i: '" + 1 + "'");
		logger.info("bd: '" + BigDecimal.ONE + "'");
		logger.info("c: '" + 'c' + "'");
		logger.info("b: '" + true + "'");
	}

	public void visit_variousTypes_shouldNotTransform(String s, int i, BigDecimal bd) {
		logger.info("s: " + "s" + " i: " + 1 + " bd: " + BigDecimal.ONE);
		logger.info("s: " + "s" + " i: " + 1 + ".");
		logger.info("s: '" + "s" + "'");
	}

	public void visit_methodsCalls_shouldTransform() {
		logger.info("Time: " + Instant.now());
		logger.info("My String " + String.format("is %s", "formatted"));
	}

	public void visit_parenthesis_shouldTransform() {
		logger.info("This " + ("is " + "Sparta"));
		logger.info("true " + (1 < 2));
		logger.info("The time is: " + ((String) Instant.now()
			.toString()).toLowerCase() + ".");
	}

	public void visit_brackets_shouldNotTransform() {
		logger.info(("This " + "is " + "Sparta"));
		logger.info(("This " + "is ") + "Sparta");
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
		// A 1 B 2 C 3 D 4
		logger.info("A " + 1 + " B {}" + " C" + " {} {}", 2, 3, "D " + 4);
	}

}
