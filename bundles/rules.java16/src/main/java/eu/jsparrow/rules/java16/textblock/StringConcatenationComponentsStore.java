package eu.jsparrow.rules.java16.textblock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Prototype without any validation.
 * <p>
 * Copied from
 * {@code eu.jsparrow.core.visitor.security.DynamicQueryComponentsStore},
 * renamed and modified.
 * 
 * @since 3.18.0
 *
 */
public class StringConcatenationComponentsStore {

	/**
	 * Recursive Method dividing a {@link String} concatenation expression into
	 * its components and storing them in a list.
	 * 
	 * @param unwrappedExpression
	 *            expected to be a {@link String} concatenation expression.
	 */
	public List<Expression> collectComponents(Expression expression) {
		Expression unwrappedExpression = removeSurroundingParenthesis(expression);
		List<Expression> componentList = new ArrayList<>();

		if (unwrappedExpression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			InfixExpression infixExpression = (InfixExpression) unwrappedExpression;
			Expression left = infixExpression.getLeftOperand();
			componentList.addAll(collectComponents(left));
			Expression right = infixExpression.getRightOperand();
			componentList.addAll(collectComponents(right));
			if (infixExpression.hasExtendedOperands()) {
				List<Expression> extendedOperands = ASTNodeUtil
					.convertToTypedList(infixExpression.extendedOperands(), Expression.class);
				extendedOperands.forEach(operand -> componentList.addAll(collectComponents(operand)));
			}
		} else {
			componentList.add(unwrappedExpression);
		}
		return componentList;
	}

	private Expression removeSurroundingParenthesis(Expression expression) {
		if (expression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression;
			return removeSurroundingParenthesis(parenthesizedExpression.getExpression());
		}
		return expression;
	}
}
