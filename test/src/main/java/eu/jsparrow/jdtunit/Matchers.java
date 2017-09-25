package eu.jsparrow.jdtunit;

import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

public final class Matchers {

	private Matchers() {

	}

	
	public static void assertMatch(Block expected, ASTNode actual) {
		ASTMatcher astMatcher = new ASTMatcher();
		assertTrue(String.format("ASTNodes do not match. Expected %s but got %s", expected, actual),
				astMatcher.match(expected, actual));
	}
}