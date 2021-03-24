package eu.jsparrow.core.visitor.junit.jupiter.assertthrows;

import eu.jsparrow.core.visitor.junit.jupiter.assertions.ReplaceJUnit4AssertionsWithJupiterASTVisitor;

/**
 * Replaces invocations of methods of the JUnit-4-class
 * {@code org.junit.Assert#assertThrows} by invocations of the corresponding
 * methods of the JUnit-Jupiter-class {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.29.0
 *
 */
public class ReplaceJUnit4AssertThrowsWithJupiterASTVisitor extends ReplaceJUnit4AssertionsWithJupiterASTVisitor {

}
