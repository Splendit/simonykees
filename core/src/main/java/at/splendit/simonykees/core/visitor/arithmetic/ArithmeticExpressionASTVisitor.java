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

	@Override
	public boolean visit(InfixExpression node) {
		if (newOperator != null) {
			return false;
		}

		InfixExpression.Operator currentOperator = node.getOperator();

		if (recursionOperator == null) {
			recursionOperator = currentOperator;
		}

		if (!hasSameOperationLevel(recursionOperator, currentOperator)) {
			return false;
		}

		if (InfixExpression.Operator.PLUS.equals(currentOperator)
				|| InfixExpression.Operator.MINUS.equals(currentOperator)
				|| InfixExpression.Operator.DIVIDE.equals(currentOperator)
				|| InfixExpression.Operator.TIMES.equals(currentOperator)) {
			
			//leftOperand all operators are legal
			if (isSimpleNameAndEqualsVarName(node.getLeftOperand())) {
				replace(node, true);
				return false;
			}
			//rightOperand & extendedOperands ony +/* are legal
			if (isSimpleNameAndEqualsVarName(node.getRightOperand())
					&& (InfixExpression.Operator.PLUS.equals(currentOperator)
							|| InfixExpression.Operator.TIMES.equals(currentOperator))) {
				replace(node, false);
				return false;
			}
			
			@SuppressWarnings("unchecked")
			List<Expression> extendedOperands = node.extendedOperands();
			
			for (Expression extendedOperand: extendedOperands){
				if (isSimpleNameAndEqualsVarName(extendedOperand)
						&& (InfixExpression.Operator.PLUS.equals(currentOperator)
								|| InfixExpression.Operator.TIMES.equals(currentOperator))) {
					newOperator = node.getOperator();
					astRewrite.getListRewrite(node , InfixExpression.EXTENDED_OPERANDS_PROPERTY).remove(extendedOperand, null);
					return false;
				}
			}
			return true;
		}
		return false;
	}
	/**	Replacement implementation for variable substitution 
	 * 
	 * @param replace node that is manipulated and got the variable as a leaf
	 * @param left true if the left leaf contains variable, otherwise false
	 */
	private void replace(InfixExpression replace, boolean left){
		newOperator = replace.getOperator();
		if (replace.extendedOperands().isEmpty()) {
			astRewrite.replace(replace, left ? replace.getRightOperand(): replace.getLeftOperand(), null);
		} else {
			if(left){
				astRewrite.replace(replace.getLeftOperand(), replace.getRightOperand(), null);
			}
			
			Expression moveTarget = (Expression) replace.extendedOperands().get(0);
			astRewrite.getListRewrite(replace , InfixExpression.EXTENDED_OPERANDS_PROPERTY).remove(moveTarget, null);
			astRewrite.replace(replace.getRightOperand(), astRewrite.createMoveTarget(moveTarget), null);
		}
	}

	public InfixExpression.Operator getNewOperator() {
		return newOperator;
	}

	private boolean isSimpleNameAndEqualsVarName(ASTNode astNode) {
		return astNode instanceof SimpleName && ((SimpleName) astNode).getIdentifier().equals(varName);
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
