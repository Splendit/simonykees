package eu.jsparrow.core.visitor.junit.jupiter;

import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Replaces each JUnit 4 annotation of the type
 * {@code org.junit.experimental.categories.Category} with one or more JUnit
 * Jupiter annotations of the type {@code org.junit.jupiter.api.Tag}.
 * 
 * @since 3.30.0
 * 
 */
public class ReplaceJUnit4CategoryWithJupiterTagASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		super.visit(compilationUnit);

		return true;
	}
}
