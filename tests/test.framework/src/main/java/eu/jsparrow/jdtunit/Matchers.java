package eu.jsparrow.jdtunit;

import static org.junit.jupiter.api.Assertions.fail;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

@SuppressWarnings("nls")
public final class Matchers {

	/*
	 * TODO: replace with a custom implementation of hamcrest's TypeSafeMatcher
	 * (https://www.baeldung.com/hamcrest-custom-matchers)
	 */
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
			fail("ASTNodes do not match. expected:\n" + expected.toString() + "\n, actual:\n" + actual.toString());
		}
	}

	public static void assertMatch(CompilationUnit expected, ASTNode actual) {
		ASTMatcher astMatcher = new ASTMatcher();
		if (!astMatcher.match(expected, actual)) {
			fail("ASTNodes do not match. expected:\n" + expected.toString() + "\n, actual:\n" + actual.toString());
		}
	}

	public static void assertMatch(TypeDeclaration expected, ASTNode actual) {
		ASTMatcher astMatcher = new ASTMatcher();
		if (!astMatcher.match(expected, actual)) {
			fail("ASTNodes do not match. expected:\n" + expected.toString() + "\n, actual:\n" + actual.toString());
		}
	}
}