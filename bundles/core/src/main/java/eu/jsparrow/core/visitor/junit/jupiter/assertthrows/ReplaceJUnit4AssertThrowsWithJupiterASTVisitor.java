package eu.jsparrow.core.visitor.junit.jupiter.assertthrows;

import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Replaces invocations of methods of the JUnit-4-class
 * {@code org.junit.Assert#assertThrows} by invocations of the corresponding
 * methods of the JUnit-Jupiter-class {@code org.junit.jupiter.api.Assertions}.
 * 
 * @since 3.29.0
 *
 */
public class ReplaceJUnit4AssertThrowsWithJupiterASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {

		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}

		return false;
	}
}
