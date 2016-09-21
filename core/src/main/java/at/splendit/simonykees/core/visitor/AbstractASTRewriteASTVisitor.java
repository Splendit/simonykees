package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

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