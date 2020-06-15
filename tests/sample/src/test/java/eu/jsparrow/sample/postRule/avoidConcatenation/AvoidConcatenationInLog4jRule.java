package eu.jsparrow.sample.postRule.avoidConcatenation;

import java.math.BigDecimal;
import java.time.Instant;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AvoidConcatenationInLog4jRule {

	private static final Logger logger = LogManager.getLogger(AvoidConcatenationInLog4jRule.class);

	/**
	 * Testing all the log levels. All statements should be transformed to use a
	 * parameter instead of the '+'
	 * 
	 * @param something
	 */
	public void visit_allLogLevels_shouldTransform(String something) {
		logger.trace("Print {}", something);
		logger.debug("Print {}", something);
		logger.info("Print {}", something);
		logger.warn("Print {}", something);
		logger.error("Print {}", something);
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
		logger.trace("Print {}", something, t);
		logger.debug("Print {}", something, e);
		logger.info("Print {}", something, r);
		logger.warn("Print {}", something, err);
		logger.error("Print {}", something, npe);
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
		logger.info("A {} B {}", 1, 2);
		logger.info("A {} B {} C {} D {}", 1, 2, 3, 4);
		logger.info("A {} B {} C {} D {} E {}", 1, 2, 3, 4, new Exception("5").getMessage());
		logger.info("A {} B {} C {} D {} E", 1, 2, 3, 4, new Exception("5"));
	}

	public void visit_variousTypes_shouldTransform(String s, int i, BigDecimal bd, char c, boolean b) {
		logger.info("s: {} i: {} bd: {} c: {} b: {}", s, i, bd, c, b);
		logger.info("s: " + "s" + " i: {}", 1);
		logger.info("i: '{}'", 1);
		logger.info("bd: '{}'", BigDecimal.ONE);
		logger.info("c: '{}'", 'c');
		logger.info("b: '{}'", true);
	}

	public void visit_variousTypes_shouldNotTransform(String s, int i, BigDecimal bd) {
		logger.info("s: '" + "s" + "'");
	}

	/**
	 * Note: This behavior is strange. This is a collection of cases where the
	 * leftOperand of an InfixExpression gets interpreted as InfixExpression
	 * instead of a StringLiteral. There are very similar cases where the
	 * leftOperand is interpreted as StringLiteral.
	 * <p/>
	 * If something breaks here, it might indicate that something in JDT
	 * changed.
	 * <p/>
	 * The problem seems to always happen under the following conditions:
	 * <ol>
	 * <li>There have to be at least 4 Expressions</li>
	 * <li>The first two have to be of node type StringLiteral</li>
	 * <li>The third has to be of something else than StringLiteral</li>
	 * <li>The fourth can be of any type</li>
	 * </ol>
	 * 
	 * @param o
	 */
	public void visit_leftOperandInfixExpression_someTransform(Object o) {
		logger.info("my " + " number " + 1 + " problem "); // no change
		logger.info("my " + " number " + " problem {}", 1); // change
		logger.info("my {} number " + " problem ", 1); // change
		logger.info("my " + " number {}", 1); // change

		logger.info("door " + " number " + 1 + 1); // no change
		logger.info("door {}{} number ", 1, 1); // change

		logger.info("my " + " number " + 1 + BigDecimal.ONE); // no change
		logger.info("1" + "1" + BigDecimal.ONE + BigDecimal.ONE); // no change
		logger.info("my {} number {}", BigDecimal.ONE, 1); // change

		logger.info("my " + " number " + o + " problem "); // no change
		logger.info("my {} number " + " problem ", o); // change
		logger.info("my number {} problem ", o); // change
		logger.info("my " + " number {}", o); // change

	}

	public void visit_methodsCalls_shouldTransform() {
		logger.info("Time: {}", Instant.now());
		logger.info("My String {}", String.format("is %s", "formatted"));
	}

	public void visit_parenthesis_shouldTransform() {
		logger.info("This {}", ("is " + "Sparta"));
		logger.info("true {}", (1 < 2));
		logger.info("The time is: {}.", ((String) Instant.now()
			.toString()).toLowerCase());
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
