package eu.jsparrow.core.visitor.arithmetic;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * This is a subtree visitor for InfixExpressions of an AssignExpression to find
 * if there is an expression that could be optimized public modifier removed,
 * because an ArithmeticExpressionASTVisitor may not be unique
 * 
 * @author Martin Huter
 * @since 0.9
 */
class ArithmeticExpressionVisitor extends ASTVisitor {

	private String varName;

	private ASTRewrite astRewrite;
	private InfixExpression.Operator newOperator;

	public ArithmeticExpressionVisitor(ASTRewrite astRewrite, SimpleName optimizationVariable) {
		super();
		this.astRewrite = astRewrite;
		newOperator = null;
		varName = optimizationVariable.getIdentifier();
	}

	@Override
	public boolean visit(InfixExpression node) {
		if (newOperator != null) {
			return false;
		}

		// only simple operations with two arguments are supported
		if (!node.extendedOperands()
			.isEmpty()) {
			return false;
		}

		InfixExpression.Operator currentOperator = node.getOperator();

		if (InfixExpression.Operator.PLUS.equals(currentOperator)
				|| InfixExpression.Operator.TIMES.equals(currentOperator)) {

			// leftOperand all operators are legal
			if (isSimpleNameAndEqualsVarName(node.getLeftOperand())
					&& !(node.getRightOperand() instanceof InfixExpression)) {
				replaceLeft(node);
				return false;
			}

			// rightOperand & extendedOperands only +/* are legal
			if (isSimpleNameAndEqualsVarName(node.getRightOperand())
					&& !(node.getLeftOperand() instanceof InfixExpression)) {
				replaceRight(node);
				return false;
			}

		} else if ((InfixExpression.Operator.MINUS.equals(currentOperator)
				|| InfixExpression.Operator.DIVIDE.equals(currentOperator))
				&& isSimpleNameAndEqualsVarName(node.getLeftOperand())
				&& !(node.getRightOperand() instanceof InfixExpression)) {
			replaceLeft(node);
			return false;
		}
		return false;
	}

	/**
	 * Introduced for better readability
	 * 
	 * @param replace
	 *            node that is manipulated and got the variable as a leaf
	 */
	private void replaceLeft(InfixExpression replace) {
		replace(replace, true);
	}

	/**
	 * Introduced for better readability
	 * 
	 * @param replace
	 *            node that is manipulated and got the variable as a leaf
	 */
	private void replaceRight(InfixExpression replace) {
		replace(replace, false);
	}

	/**
	 * Replacement implementation for variable substitution
	 * 
	 * @param replace
	 *            node that is manipulated and got the variable as a leaf
	 * @param left
	 *            true if the left leaf contains variable, otherwise false
	 */
	private void replace(InfixExpression replace, boolean left) {
		newOperator = replace.getOperator();
		if (replace.extendedOperands()
			.isEmpty()) {
			astRewrite.replace(replace, left ? replace.getRightOperand() : replace.getLeftOperand(), null);
		} else {
			if (left) {
				astRewrite.replace(replace.getLeftOperand(), replace.getRightOperand(), null);
			}

			Expression moveTarget = (Expression) replace.extendedOperands()
				.get(0);
			astRewrite.getListRewrite(replace, InfixExpression.EXTENDED_OPERANDS_PROPERTY)
				.remove(moveTarget, null);
			astRewrite.replace(replace.getRightOperand(), astRewrite.createMoveTarget(moveTarget), null);
		}
	}

	public InfixExpression.Operator getNewOperator() {
		return newOperator;
	}

	private boolean isSimpleNameAndEqualsVarName(ASTNode astNode) {
		return astNode instanceof SimpleName && ((SimpleName) astNode).getIdentifier()
			.equals(varName);
	}
}
