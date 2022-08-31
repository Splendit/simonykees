package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class EqualityOperationForSwitch {

	private final SimpleName variableForSwitch;
	private final Expression caseExpression;
	private final int operationNodeType;

	static Optional<EqualityOperationForSwitch> findEqualityOperationForSwitch(InfixExpression infixExpression) {
		if (infixExpression.hasExtendedOperands()) {
			return Optional.empty();
		}
		Operator operator = infixExpression.getOperator();
		if (operator != Operator.EQUALS) {
			return Optional.empty();
		}
		Expression leftOperand = infixExpression.getLeftOperand();
		Expression rightOperand = infixExpression.getRightOperand();
		return findEqualityOperationForSwitch(leftOperand, rightOperand, ASTNode.INFIX_EXPRESSION);
	}

	static Optional<EqualityOperationForSwitch> findEqualityOperationForSwitch(MethodInvocation methodInvocation) {
		Expression invocationExpression = methodInvocation.getExpression();
		if (invocationExpression == null) {
			return Optional.empty();
		}
		Expression equalsMethodArgument = findEqualsMethodArgument(methodInvocation).orElse(null);
		if(equalsMethodArgument == null) {
			return Optional.empty();
		}
		return findEqualityOperationForSwitch(invocationExpression, equalsMethodArgument, ASTNode.METHOD_INVOCATION);
	}

	static Optional<Expression> findEqualsMethodArgument(MethodInvocation methodInvocation) {
		if (!methodInvocation.getName()
			.getIdentifier()
			.equals("equals")) {
			return Optional.empty();
		}
		List<Expression> invocationArgumentList = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);
		if (invocationArgumentList.size() != 1) {
			return Optional.empty();
		}
		return Optional.of(invocationArgumentList.get(0));
	}

	private static Optional<EqualityOperationForSwitch> findEqualityOperationForSwitch(Expression leftOperand,
			Expression rightOperand, int operationNodeType) {

		if (leftOperand.getNodeType() == ASTNode.SIMPLE_NAME) {
			return Optional
				.of(new EqualityOperationForSwitch((SimpleName) leftOperand, rightOperand, operationNodeType));
		}

		if (rightOperand.getNodeType() == ASTNode.SIMPLE_NAME) {
			return Optional
				.of(new EqualityOperationForSwitch((SimpleName) rightOperand, leftOperand, operationNodeType));
		}

		return Optional.empty();
	}

	private EqualityOperationForSwitch(SimpleName variableForSwitch, Expression literalExpression,
			int operationNodeType) {

		this.variableForSwitch = variableForSwitch;
		this.caseExpression = literalExpression;
		this.operationNodeType = operationNodeType;
	}

	SimpleName getVariableForSwitch() {
		return variableForSwitch;
	}

	Expression getCaseExpression() {
		return caseExpression;
	}

	/**
	 * 
	 * @return if the parent of the operands is a {@link MethodInvocation}, then
	 *         {@link ASTNode#METHOD_INVOCATION} is returned, otherwise the
	 *         parent of the operands can only be an {@link InfixExpression} and
	 *         therefore {@link ASTNode#INFIX_EXPRESSION} is returned.
	 */
	public int getOperationNodeType() {
		return operationNodeType;
	}
}
