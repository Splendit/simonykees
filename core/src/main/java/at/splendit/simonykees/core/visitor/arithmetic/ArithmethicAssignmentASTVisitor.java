package at.splendit.simonykees.core.visitor.arithmetic;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;

import at.splendit.simonykees.core.helper.ArithmeticHelper;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;


/**
 * This visitor is build for assignments of arithmetic expressions for base numeric types
 * 
 * 
 * Examples: 
 * 
 * a = a + 3; => a += 3;
 * @author mgh
 *
 */
public class ArithmethicAssignmentASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(Assignment node) {
		if (node.getOperator() != null && node.getOperator().equals(Operator.ASSIGN)) {
			if (node.getLeftHandSide() instanceof SimpleName && node.getRightHandSide() instanceof InfixExpression) {

				Assignment replacementNode = (Assignment) ASTNode.copySubtree(node.getAST(), node);
				SimpleName leftHandSide = (SimpleName) replacementNode.getLeftHandSide();
				//TODO: check if the leftHandSide is a basic arithmetic type.
				// String concatenations could be messed up by optimization. 
				InfixExpression rightHandSide = (InfixExpression) replacementNode.getRightHandSide();
				
				ArithmeticExpressionASTVisitor arithExpASTVisitor = new ArithmeticExpressionASTVisitor(astRewrite, leftHandSide);
				
				rightHandSide.accept(arithExpASTVisitor);
				
				if(arithExpASTVisitor.getNewOperator() != null){
					replacementNode.setOperator(ArithmeticHelper.generateOperator(arithExpASTVisitor.getNewOperator()));					
					astRewrite.replace(node, replacementNode, null);
				}
				else {
					replacementNode.delete();
				}
			}
		}
		return true;
	}
}
