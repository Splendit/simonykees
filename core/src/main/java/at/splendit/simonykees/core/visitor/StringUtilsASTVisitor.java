package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class StringUtilsASTVisitor extends ASTVisitor {

	private ASTRewrite astRewrite;

	public StringUtilsASTVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}
	

}
