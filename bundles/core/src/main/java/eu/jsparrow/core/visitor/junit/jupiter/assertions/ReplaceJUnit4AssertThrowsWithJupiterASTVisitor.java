package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import static eu.jsparrow.core.visitor.junit.jupiter.assertions.JUnit4AssertMethodInvocationAnalyzer.ASSERT_THROWS;

import java.util.function.Predicate;

/**
 * Replaces invocations of methods of the JUnit-4-class
 * {@code org.junit.Assert#assertThrows} by invocations of the corresponding
 * methods of the JUnit-Jupiter-class {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.29.0
 *
 */
public class ReplaceJUnit4AssertThrowsWithJupiterASTVisitor extends ReplaceJUnit4AssertionsWithJupiterASTVisitor {
	@Override
	protected Predicate<String> getMethodNamePredicate() {
		return ASSERT_THROWS::equals;
	}
}
