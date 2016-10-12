package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Abstract implementation of an {@link ASTVisitor} to assure all used visitors
 * have an field for {@link ASTRewrite} to commit the changes in the tree.
 * 
 * @author Martin Huter
 *
 */

public abstract class AbstractASTRewriteASTVisitor extends ASTVisitor {

	protected ASTRewrite astRewrite;

	public AbstractASTRewriteASTVisitor() {
		super();
	}

	public AbstractASTRewriteASTVisitor(boolean visitDocTags) {
		super(visitDocTags);
	}

	public ASTRewrite getAstRewrite() {
		return astRewrite;
	}

	public void setAstRewrite(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}

}