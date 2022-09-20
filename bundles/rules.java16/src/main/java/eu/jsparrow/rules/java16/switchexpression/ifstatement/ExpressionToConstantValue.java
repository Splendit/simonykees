package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class which tries to extract an Integer value from an
 * {@link Expression}.
 * <p>
 * For example, it is possible to extract the Integer with the value 1 from an
 * infix expression node like {code +1 }.
 * 
 * 
 * @since 4.3.0
 */
public class ExpressionToConstantValue {

	/**
	 * 
	 * @param expression
	 *            expected to be a numeric int expression.
	 * @return an {@link Optional} storing an Integer which corresponds to the
	 *         numeric expression, or an empty {@link Optional} if no valid
	 *         integer value could be extracted.
	 */
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
