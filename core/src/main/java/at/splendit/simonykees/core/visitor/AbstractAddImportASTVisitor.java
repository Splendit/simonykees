package at.splendit.simonykees.core.visitor;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * Extended {@link AbstractASTRewriteASTVisitor} where a list of java classes
 * can be injected by fully qualified name to enable a comparison.
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public abstract class AbstractAddImportASTVisitor extends AbstractASTRewriteASTVisitor {

	protected static final String JAVA_LANG_PACKAGE = "java.lang"; //$NON-NLS-1$

	protected Set<String> addImports;

	protected AbstractAddImportASTVisitor() {
		super();
		this.addImports = new HashSet<>();
	}

	@SuppressWarnings("unchecked")
	public void endVisit(CompilationUnit node) {

		/**
		 * Manages the addition of new Imports
		 */
		for (String iterator : addImports) {
			/**
			 * java.lang doesn't need to be imported
			 */
			if (!StringUtils.startsWith(iterator, JAVA_LANG_PACKAGE)) {
				ImportDeclaration newImport = node.getAST().newImportDeclaration();
				newImport.setName(node.getAST().newName(iterator));
				if (node.imports().stream().noneMatch(importDeclaration -> (new ASTMatcher())
						.match((ImportDeclaration) importDeclaration, newImport))) {
					astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY).insertLast(newImport, null);
				}
			}
		}
	}
}
