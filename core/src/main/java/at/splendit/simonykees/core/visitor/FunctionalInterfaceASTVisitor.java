package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;

public class FunctionalInterfaceASTVisitor extends AbstractASTRewriteASTVisitor {
	
	@Override
	public boolean visit(AnonymousClassDeclaration node){
		ClassInstanceCreation parentNode = (ClassInstanceCreation) node.getParent();
		ITypeBinding parentNodeTypeBinding = parentNode.getType().resolveBinding();
		if (parentNodeTypeBinding != null){
			if(parentNodeTypeBinding.getFunctionalInterfaceMethod() != null){
				LambdaExpression newInitializer = node.getAST().newLambdaExpression();
				MethodBlockASTVisitor methodBlockASTVisitor = new MethodBlockASTVisitor();
				methodBlockASTVisitor.setAstRewrite(astRewrite);
				node.accept(methodBlockASTVisitor);
				Block moveBlock = methodBlockASTVisitor.getMethodBlock();
				if(moveBlock != null){
					newInitializer.setBody(moveBlock);
					getAstRewrite().replace(parentNode, newInitializer, null);
				}
			}
		}
		return true;
	}
	
	private class MethodBlockASTVisitor extends AbstractASTRewriteASTVisitor {
		private Block methodBlock = null;

		@Override
		public boolean visit(Block node){
			methodBlock = (Block) getAstRewrite().createMoveTarget(node);
			getAstRewrite().remove(node, null);
			return false;
		}

		public Block getMethodBlock() {
			return methodBlock;
		}
	}
}
