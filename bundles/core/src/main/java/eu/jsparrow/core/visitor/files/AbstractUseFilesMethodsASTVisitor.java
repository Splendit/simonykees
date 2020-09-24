package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ClassRelationUtil;
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

	/**
	 * 
	 * @return If the variable declared by the given
	 *         {@link VariableDeclarationFragment} has the specified type and is
	 *         also initialized with a constructor of the specified type, then
	 *         the corresponding {@link ClassInstanceCreation} is returned.
	 *         Otherwise, null is returned.
	 */
	protected ClassInstanceCreation findClassInstanceCreationAsInitializer(VariableDeclarationFragment fragment,
			String qualifiedClassName) {
		SimpleName name = fragment.getName();
		ITypeBinding typeBinding = name.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, qualifiedClassName)) {
			return null;
		}

		Expression initializer = fragment.getInitializer();
		if (!ClassRelationUtil.isNewInstanceCreationOf(initializer, qualifiedClassName)) {
			return null;
		}
		return (ClassInstanceCreation) initializer;
	}
}
