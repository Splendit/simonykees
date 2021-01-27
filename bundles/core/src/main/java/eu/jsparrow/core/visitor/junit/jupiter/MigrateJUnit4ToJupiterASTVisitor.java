package eu.jsparrow.core.visitor.junit.jupiter;

import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * @since 3.27.0
 *
 */
public class MigrateJUnit4ToJupiterASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		return continueVisiting;
	}

}
