package eu.jsparrow.core.visitor.junit.dedicated;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression.Operator;

/**
 * 
 * Expressions like for example
 * <p>
 * {@code !(a==b)}
 * <p>
 * are unwrapped to
 * <p>
 * {@code a==b}
 * <p>
 * and a flag will indicate if a negation has been found.
 * 
 * @since 4.0.0
 */
public class NotOperandUnwrapper {
	private boolean negationByNot;
	private Expression unwrappedOperand;

	public NotOperandUnwrapper(Expression booleanExpression) {
		unwrappedOperand = unwrapExpression(booleanExpression);
	}

	private Expression unwrapExpression(Expression expression) {
		if (expression.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression prefixExpression = (PrefixExpression) expression;
			if (prefixExpression.getOperator() == Operator.NOT) {
				negationByNot = !negationByNot;
				return unwrapExpression(prefixExpression.getOperand());
			}
		}
		if (expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
			return unwrapExpression(parenthesizedExpression.getExpression());
		}
		return expression;
	}

	public boolean isNegationByNot() {
		return negationByNot;
	}

	public Expression getUnwrappedOperand() {
		return unwrappedOperand;
	}
}
