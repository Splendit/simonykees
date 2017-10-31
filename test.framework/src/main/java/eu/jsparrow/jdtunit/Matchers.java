package eu.jsparrow.jdtunit;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

import junit.framework.ComparisonFailure;

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
			throw new ComparisonFailure("ASTNodes do not match.", expected.toString(), actual.toString()); //$NON-NLS-1$
		}
	}
}