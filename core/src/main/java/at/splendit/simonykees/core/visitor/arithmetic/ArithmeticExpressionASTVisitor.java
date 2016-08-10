package at.splendit.simonykees.core.visitor.arithmetic;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ArithmeticExpressionASTVisitor extends ASTVisitor {
	
	private String varName;
	
	private ASTRewrite astRewrite;
	private InfixExpression.Operator newOperator;
	
	public ArithmeticExpressionASTVisitor(ASTRewrite astRewrite, SimpleName optimizationVariable){
		super();
		this.astRewrite = astRewrite;
		newOperator = null;
		varName = optimizationVariable.getIdentifier();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean visit(InfixExpression node) {
		if(newOperator != null){
			return false;
		}
		
		Expression infixLeftOperand = node.getLeftOperand();
		Expression infixRightOperand = node.getRightOperand();
		InfixExpression.Operator currentOperator = node.getOperator();
		List<Expression> extendedOperands = node.extendedOperands();

		if (InfixExpression.Operator.PLUS.equals(currentOperator) || 
				InfixExpression.Operator.MINUS.equals(currentOperator)) {
			if (infixLeftOperand instanceof SimpleName) {
				SimpleName simpleLeftOperand = (SimpleName) infixLeftOperand;
				if (simpleLeftOperand.getIdentifier().equals(varName)) {
					newOperator = currentOperator;
					if(extendedOperands.isEmpty()){
						astRewrite.replace(node, infixRightOperand, null);
					}
					else {
						InfixExpression replacement = node.getAST().newInfixExpression();
						Expression firstAdditional = extendedOperands.remove(0);
						astRewrite.replace(infixLeftOperand, infixRightOperand, null);
						Expression newInfixRightOperand =(Expression) ASTNode.copySubtree(infixRightOperand.getAST(), infixRightOperand);
						replacement.setOperator(currentOperator);
						replacement.setLeftOperand(newInfixRightOperand);
						replacement.setRightOperand(firstAdditional);
						replacement.extendedOperands().addAll(extendedOperands);
						astRewrite.replace(node, replacement, null);
					}
					return false;
				}
			} else if (infixLeftOperand instanceof InfixExpression) {
				return true;
			}
			//Other Types of nodes are not relevant for this use case 
			return true;
		} else if (InfixExpression.Operator.DIVIDE.equals(currentOperator) ||
				InfixExpression.Operator.TIMES.equals(currentOperator)) {
			throw new RuntimeException("NotYetImplemented");
		}
		return false;
	}
	
	public InfixExpression.Operator getNewOperator() {
		return newOperator;
	}
}
