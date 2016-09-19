package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

import at.splendit.simonykees.core.Activator;

public class FunctionalInterfaceASTVisitor extends ASTVisitor {
	
	ASTRewrite astRewrite;

	public FunctionalInterfaceASTVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration node){
		ClassInstanceCreation parentNode = (ClassInstanceCreation) node.getParent();
		ITypeBinding parentNodeTypeBinding = parentNode.getType().resolveBinding();
		if (parentNodeTypeBinding != null){
			if(parentNodeTypeBinding.getFunctionalInterfaceMethod() != null){
				LambdaExpression newInitializer = node.getAST().newLambdaExpression();
				MethodBlockASTVisitor methodBlockASTVisitor = new MethodBlockASTVisitor(astRewrite);
				node.accept(methodBlockASTVisitor);
				Block moveBlock = methodBlockASTVisitor.getMethodBlock();
				if(moveBlock != null){
					newInitializer.setBody(moveBlock);
					astRewrite.replace(parentNode, newInitializer, null);
				}
			}
		}
		return true;
	}

	
	private class MethodBlockASTVisitor extends ASTVisitor {
		ASTRewrite astRewrite;
		private Block methodBlock = null;

		public MethodBlockASTVisitor(ASTRewrite astRewrite) {
			this.astRewrite = astRewrite;
		}
		
		@Override
		public boolean visit(Block node){
			methodBlock = (Block) astRewrite.createMoveTarget(node);
			astRewrite.remove(node, null);
			return false;
		}

		public Block getMethodBlock() {
			return methodBlock;
		}
	}
}
