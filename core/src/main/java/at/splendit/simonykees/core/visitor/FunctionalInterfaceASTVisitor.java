package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import at.splendit.simonykees.core.Activator;

public class FunctionalInterfaceASTVisitor extends ASTVisitor {
	
	ASTRewrite astRewrite;

	public FunctionalInterfaceASTVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}
	
	@Override
	public boolean visit(CompilationUnit node){
		Activator.log("lalelu");
		return true;
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node){
		Activator.log("lalelu");
		return true;
	}
	
	@Override
	public boolean visit(ClassInstanceCreation node){
		if(node.getAnonymousClassDeclaration() != null){
			Activator.log("lalelu");
		}
		Activator.log("lalelu");
		return true;
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration node){
		Activator.log("lalelu");
		return true;
	}

}
