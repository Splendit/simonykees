package at.splendit.simonykees.core.visitor.arithmetic;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import at.splendit.simonykees.core.helper.ArithmeticHelper;

public class ArithmethicAssignmentASTVisitor extends ASTVisitor {

	private ASTRewrite astRewrite;

	public ArithmethicAssignmentASTVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}

	@Override
	public boolean visit(Assignment node) {
		if (node.getOperator() != null && node.getOperator().equals(Operator.ASSIGN)) {
			if (node.getLeftHandSide() instanceof SimpleName && node.getRightHandSide() instanceof InfixExpression) {

				Assignment replacementNode = (Assignment) ASTNode.copySubtree(node.getAST(), node);
				SimpleName leftHandSide = (SimpleName) replacementNode.getLeftHandSide();
				InfixExpression rightHandSide = (InfixExpression) replacementNode.getRightHandSide();
				
				ArithmeticExpressionASTVisitor arithExpASTVisitor = new ArithmeticExpressionASTVisitor(astRewrite, leftHandSide);
				
				rightHandSide.accept(arithExpASTVisitor);
				
				
				//Pair<InfixExpression, Expression> nodesToChange = ArithmeticHelper.extractSimpleName(leftHandSide,
				//		rightHandSide);

				if(arithExpASTVisitor.getNewOperator() != null){
					replacementNode.setOperator(ArithmeticHelper.generateOperator(arithExpASTVisitor.getNewOperator()));					
					astRewrite.replace(node, replacementNode, null);
				}
			}
		}
		return true;
	}

	
}
