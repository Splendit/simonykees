package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Replaces the JUnit 4 method invocations
 * {@code org.junit.Assume.assumeNoException},
 * {@code org.junit.Assume.assumeNotNull} and
 * {@code org.junit.Assume.assumeThat} by invocations of the corresponding
 * methods of {@code org.hamcrest.junit.MatcherAssume.assumeThat}.
 * 
 * @since 3.31.0
 * 
 */
public class ReplaceJUnit4AssumptionsWithHamcrestJUnitASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		return super.visit(compilationUnit);
	}
}