package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

/**
 * Finds anonymous classes an converts it to lambdas, if they are functional
 * interfaces.
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class FunctionalInterfaceASTVisitor extends AbstractASTRewriteASTVisitor {

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (ASTNode.CLASS_INSTANCE_CREATION == node.getParent().getNodeType()) {
			ClassInstanceCreation parentNode = (ClassInstanceCreation) node.getParent();
			Type parentType = parentNode.getType();
			if(ASTNode.PARAMETERIZED_TYPE != parentType.getNodeType()) {
				ITypeBinding parentNodeTypeBinding = parentNode.getType().resolveBinding();
				if (parentNodeTypeBinding != null) {
					if (parentNodeTypeBinding.getFunctionalInterfaceMethod() != null) {
						LambdaExpression newInitializer = node.getAST().newLambdaExpression();
						MethodBlockASTVisitor methodBlockASTVisitor = new MethodBlockASTVisitor();
						node.accept(methodBlockASTVisitor);
						Block moveBlock = methodBlockASTVisitor.getMethodBlock();
						if (moveBlock != null) {
							if (methodBlockASTVisitor.getParameters() != null) {
								for (SingleVariableDeclaration s : methodBlockASTVisitor.getParameters()) {
									newInitializer.parameters().add(astRewrite.createMoveTarget(s));
								}
							}
							newInitializer.setBody(astRewrite.createMoveTarget(moveBlock));
							getAstRewrite().replace(parentNode, newInitializer, null);
						}
					}
				}
			}
		}
		return true;
	}

	private class MethodBlockASTVisitor extends ASTVisitor {
		private Block methodBlock = null;
		private List<SingleVariableDeclaration> parameters = null;

		@Override
		public boolean visit(Block node) {
			methodBlock = (Block) getAstRewrite().createMoveTarget(node);
			getAstRewrite().remove(node, null);
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean visit(MethodDeclaration node) {
			if (!node.parameters().isEmpty()) {
				/**
				 * node.parameters() ensures that the List contains only
				 * SingleVariableDeclaration
				 */
				parameters = node.parameters();
			}
			methodBlock = node.getBody();
			return false;
		}

		public Block getMethodBlock() {
			return methodBlock;
		}

		public List<SingleVariableDeclaration> getParameters() {
			return parameters;
		}
	}
}
