package at.splendit.simonykees.core.visitor.arithmetic;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * This is a subtree visitor for InfixExpressions of an AssignExpression to find if there is an
 * expression that could be optimized
 * public modifier removed, because an ArithmeticExpressionASTVisitor may not be unique
 * @author mgh
 *
 */
class ArithmeticExpressionASTVisitor extends ASTVisitor {

	private String varName;

	private ASTRewrite astRewrite;
	private InfixExpression.Operator newOperator;
	private InfixExpression.Operator recursionOperator;

	public ArithmeticExpressionASTVisitor(ASTRewrite astRewrite, SimpleName optimizationVariable) {
		super();
		this.astRewrite = astRewrite;
		newOperator = null;
		varName = optimizationVariable.getIdentifier();
	}

	// extendedOperands are unchecked
	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(InfixExpression node) {
		if (newOperator != null) {
			return false;
		}

		Expression infixLeftOperand = node.getLeftOperand();
		Expression infixRightOperand = node.getRightOperand();
		InfixExpression.Operator currentOperator = node.getOperator();

		if (recursionOperator == null) {
			recursionOperator = currentOperator;
		}

		if (!hasSameOperationLevel(recursionOperator, currentOperator)) {
			return false;
		}

		List<Expression> extendedOperands = node.extendedOperands();

		if (InfixExpression.Operator.PLUS.equals(currentOperator)
				|| InfixExpression.Operator.MINUS.equals(currentOperator)
				|| InfixExpression.Operator.DIVIDE.equals(currentOperator)
				|| InfixExpression.Operator.TIMES.equals(currentOperator)) {

			if (isSimpleNameAndEqualsVarName(infixLeftOperand)) {
				newOperator = currentOperator;
				if (extendedOperands.isEmpty()) {
					astRewrite.replace(node, infixRightOperand, null);
				} else {
					// Moving all child nodes one leaf left if extendedOperands
					// are present
					InfixExpression replacement = node.getAST().newInfixExpression();
					Expression firstAdditional = extendedOperands.remove(0);
					astRewrite.replace(infixLeftOperand, infixRightOperand, null);
					Expression newInfixRightOperand = (Expression) ASTNode.copySubtree(infixRightOperand.getAST(),
							infixRightOperand);
					replacement.setOperator(currentOperator);
					replacement.setLeftOperand(newInfixRightOperand);
					replacement.setRightOperand(firstAdditional);
					replacement.extendedOperands().addAll(extendedOperands);
					astRewrite.replace(node, replacement, null);
				}
				return false;
			}
			if (isSimpleNameAndEqualsVarName(infixRightOperand)
					&& (InfixExpression.Operator.PLUS.equals(currentOperator)
							|| InfixExpression.Operator.TIMES.equals(currentOperator))) {
				newOperator = currentOperator;
				if (extendedOperands.isEmpty()) {
					astRewrite.replace(node, infixLeftOperand, null);
				} else {
					Expression firstAdditional = extendedOperands.remove(0);
					astRewrite.replace(infixRightOperand, firstAdditional, null);
				}
				return false;
			}

			// Other Types of nodes are not relevant for this use case
			return true;
		}
		return false;
	}

	public InfixExpression.Operator getNewOperator() {
		return newOperator;
	}

	private boolean isSimpleNameAndEqualsVarName(Expression expression) {
		return expression instanceof SimpleName && ((SimpleName) expression).getIdentifier().equals(varName);
	}

	private boolean hasSameOperationLevel(InfixExpression.Operator operator1, InfixExpression.Operator operator2) {
		if (operator1 == null || operator2 == null) {
			return false;
		}
		if ((InfixExpression.Operator.PLUS.equals(operator1) || InfixExpression.Operator.MINUS.equals(operator1))
				&& (InfixExpression.Operator.PLUS.equals(operator2)
						|| InfixExpression.Operator.MINUS.equals(operator2))) {
			return true;
		}
		if ((InfixExpression.Operator.TIMES.equals(operator1) || InfixExpression.Operator.DIVIDE.equals(operator1))
				&& (InfixExpression.Operator.TIMES.equals(operator2)
						|| InfixExpression.Operator.DIVIDE.equals(operator2))) {
			return true;
		}
		return false;
	}
}
