package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class ExpressionToConstantValue {

	public static Optional<Integer> extractIntegerConstant(Expression expression) {
		return findNumericToken(expression, Operator.PLUS).map(Integer::decode);
	}

	private static Optional<String> findNumericToken(Expression expression, Operator operator) {

		expression = ASTNodeUtil.unwrapParenthesizedExpression(expression);

		if (expression.getNodeType() == ASTNode.NUMBER_LITERAL) {
			String numericToken = ((NumberLiteral) expression).getToken();
			if (operator == Operator.MINUS) {
				return Optional.of("-" + numericToken); //$NON-NLS-1$
			}
			return Optional.of(numericToken);
		}

		if (expression.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression prefixExpression = (PrefixExpression) expression;
			Operator newOperator = findNewPrefixOperator(operator, prefixExpression.getOperator()).orElse(null);
			if (newOperator != null) {
				return findNumericToken(prefixExpression.getOperand(), newOperator);
			}
		}
		return Optional.empty();
	}

	private static Optional<Operator> findNewPrefixOperator(Operator operator, Operator operatorNew) {
		if (operator == Operator.MINUS) {
			if (operatorNew == Operator.MINUS) {
				return Optional.of(Operator.PLUS);
			}
			if (operatorNew == Operator.PLUS) {
				return Optional.of(Operator.MINUS);
			}
		}

		if (operator == Operator.PLUS) {
			if (operatorNew == Operator.MINUS) {
				return Optional.of(Operator.MINUS);
			}
			if (operatorNew == Operator.PLUS) {
				return Optional.of(Operator.PLUS);
			}
		}
		return Optional.empty();
	}

	private ExpressionToConstantValue() {
		/*
		 * private default constructor hiding implicit public one
		 */
	}

}
