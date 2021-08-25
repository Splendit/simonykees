package eu.jsparrow.rules.java16.textblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

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
	private List<Expression> collectComponents(Expression expression) {
		Expression unwrappedExpression = removeSurroundingParenthesis(expression);
		if (unwrappedExpression.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			return collectConcatenationComponents((InfixExpression) unwrappedExpression);
		}
		if (unwrappedExpression.getNodeType() == ASTNode.STRING_LITERAL ||
				unwrappedExpression.getNodeType() == ASTNode.NUMBER_LITERAL ||
				unwrappedExpression.getNodeType() == ASTNode.CHARACTER_LITERAL ||
				unwrappedExpression.getNodeType() == ASTNode.BOOLEAN_LITERAL ||
				unwrappedExpression.getNodeType() == ASTNode.NULL_LITERAL) {
			return Collections.singletonList(unwrappedExpression);
		}
		return Collections.emptyList();
	}

	public List<Expression> collectConcatenationComponents(InfixExpression infixExpression) {
		Operator operator = infixExpression.getOperator();
		if (operator != InfixExpression.Operator.PLUS) {
			return Collections.emptyList();
		}

		Expression left = infixExpression.getLeftOperand();
		Expression right = infixExpression.getRightOperand();
		if (!isStringOperand(left) && !isStringOperand(right)) {
			return Collections.emptyList();
		}
		List<Expression> leftOperandComponents = collectComponents(left);
		if(leftOperandComponents.isEmpty()) {
			return Collections.emptyList();
		}
		List<Expression> rightOperandComponents = collectComponents(right);
		if(rightOperandComponents.isEmpty()) {
			return Collections.emptyList();
		}


		List<Expression> componentList = new ArrayList<>();		
		componentList.addAll(leftOperandComponents);
		componentList.addAll(rightOperandComponents);
		if (infixExpression.hasExtendedOperands()) {
			List<Expression> extendedOperands = ASTNodeUtil
				.convertToTypedList(infixExpression.extendedOperands(), Expression.class);
			for(Expression operand : extendedOperands) {
				List<Expression> operandComponents = collectComponents(operand);
				if(operandComponents.isEmpty()) {
					return Collections.emptyList();
				}
				componentList.addAll(operandComponents);
			}
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

	private boolean isStringOperand(Expression expression) {
		return ClassRelationUtil.isContentOfType(expression.resolveTypeBinding(), String.class.getName());
	}
}
