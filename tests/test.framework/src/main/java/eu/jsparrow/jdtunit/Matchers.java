package eu.jsparrow.jdtunit;


import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

public final class Matchers {

	private Matchers() {

	}

	/**
	 * Asserts that two instances of {@link ASTNode} are equal using ASTMatcher.
	 * Throws an {@link AssertionError} if they are not.
	 * 
	 * @param expected
	 *            the {@link Block} to compare with
	 * @param actual
	 *            the actual block
	 */
	public static void assertMatch(Block expected, ASTNode actual) {
		ASTMatcher astMatcher = new ASTMatcher();
		if (!astMatcher.match(expected, actual)) {
			fail("ASTNodes do not match. expected:" + expected.toString() +", actual:"+ actual.toString()); //$NON-NLS-1$
		}
	}
}