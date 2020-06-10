package eu.jsparrow.sample.postRule.allRules;

import java.math.BigDecimal;
import java.time.Instant;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AvoidEvaluationOfParametersInSlf4jRule {

	private static final Logger logger = LoggerFactory.getLogger(AvoidEvaluationOfParametersInSlf4jRule.class);

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
			Error err, NullPointerException npe) {
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
		logger.info(new StringBuilder().append("s: ")
			.append("s")
			.append(" i: {}")
			.toString(), 1);
		logger.info("i: '{}'", 1);
		logger.info("bd: '{}'", BigDecimal.ONE);
		logger.info("c: '{}'", 'c');
		logger.info("b: '{}'", true);
	}

	public void visit_variousTypes_shouldNotTransform(String s, int i, BigDecimal bd) {
		logger.info(new StringBuilder().append("s: '")
			.append("s")
			.append("'")
			.toString());
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
		logger.info(new StringBuilder().append("my ")
			.append(" number ")
			.append(1)
			.append(" problem ")
			.toString()); // no change
		logger.info(new StringBuilder().append("my ")
			.append(" number ")
			.append(" problem {}")
			.toString(), 1); // change
		logger.info("my {} number " + " problem ", 1); // change
		logger.info("my " + " number {}", 1); // change

		logger.info(new StringBuilder().append("door ")
			.append(" number ")
			.append(1)
			.append(1)
			.toString()); // no change
		logger.info("door {}{} number ", 1, 1); // change

		logger.info(new StringBuilder().append("my ")
			.append(" number ")
			.append(1)
			.append(BigDecimal.ONE)
			.toString()); // no change
		logger.info(new StringBuilder().append("1")
			.append("1")
			.append(BigDecimal.ONE)
			.append(BigDecimal.ONE)
			.toString()); // no change
		logger.info("my {} number {}", BigDecimal.ONE, 1); // change

		logger.info(new StringBuilder().append("my ")
			.append(" number ")
			.append(o)
			.append(" problem ")
			.toString()); // no change
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
		logger.info("The time is: {}.", StringUtils.lowerCase(Instant.now()
			.toString()));
	}

	public void visit_brackets_shouldNotTransform() {
		logger.info((new StringBuilder().append("This ")
			.append("is ")
			.append("Sparta")
			.toString()));
		logger.info(new StringBuilder().append("This ")
			.append("is ")
			.append("Sparta")
			.toString());
	}

	/**
	 * None of the statements should be transformed, since they already contain
	 * a parameter.
	 * 
	 * @param something
	 */
	public void logSomething_noChange_containsArgument(String b, String c, Exception e) {
		logger.info(new StringBuilder().append("A ")
			.append(1)
			.append(" B {}")
			.append(2)
			.toString());
		logger.info(new StringBuilder().append("A ")
			.append(1)
			.append(" B {}")
			.toString(), 2);
		logger.info("A {} " + c, b); // A {b} {c}
		logger.error("A {} " + c, b, e); // A {b} {c} {e}
		logger.info(new StringBuilder().append("A {} ")
			.append("C")
			.append(" {}")
			.toString(), "B", "D"); // A B C D
		logger.info(new StringBuilder().append("A ")
			.append("B ")
			.append(c)
			.append(" {}")
			.toString(), "D"); // A B {c} D
		logger.info(new StringBuilder().append("A {} ")
			.append(c)
			.append(" {}")
			.toString(), "B", "D", e); // A B {c} D {e}
		logger.info(new StringBuilder().append("A ")
			.append("B ")
			.append(c)
			.append(" {}")
			.toString(), "D", e); // A B {c} D {e}
		logger.info(new StringBuilder().append("A ")
			.append(1)
			.append(" B {}")
			.toString(), 2); // A 1 B 2
		// A 1 B 2 C 3 D 4
		logger.info(new StringBuilder().append("A ")
			.append(1)
			.append(" B {}")
			.append(" C")
			.append(" {} {}")
			.toString(), 2, 3, "D " + 4);
	}

}
