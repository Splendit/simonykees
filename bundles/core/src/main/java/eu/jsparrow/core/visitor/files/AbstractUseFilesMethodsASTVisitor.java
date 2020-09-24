package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Parent class for visitors which transform code in order to use methods of the
 * class {@link java.nio.file.Files} for the creation of objects like for
 * example instances of <br>
 * {@link java.io.BufferedReader}<br>
 * or <br>
 * {@link java.io.BufferedWriter}
 * 
 * @since 3.22.0
 *
 */
abstract class AbstractUseFilesMethodsASTVisitor extends AbstractAddImportASTVisitor {
	protected static final String FILES_QUALIFIED_NAME = java.nio.file.Files.class.getName();
	protected static final String PATHS_QUALIFIED_NAME = java.nio.file.Paths.class.getName();
	protected static final String CHARSET_QUALIFIED_NAME = java.nio.charset.Charset.class.getName();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}
		verifyImport(compilationUnit, PATHS_QUALIFIED_NAME);
		verifyImport(compilationUnit, FILES_QUALIFIED_NAME);
		verifyImport(compilationUnit, CHARSET_QUALIFIED_NAME);
		return continueVisiting;
	}
}
