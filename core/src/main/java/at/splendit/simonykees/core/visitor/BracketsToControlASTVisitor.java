package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class BracketsToControlASTVisitor extends ASTVisitor {

	ASTRewrite astRewrite;

	public BracketsToControlASTVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}
	
	@Override	
	public boolean visit(EnhancedForStatement node) {
		return true;
	}
	
	@Override
	public boolean visit(CompilationUnit node) {
		return true;
	}

	
	
}
