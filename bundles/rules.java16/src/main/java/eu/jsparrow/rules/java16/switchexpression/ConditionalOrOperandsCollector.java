package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class ConditionalOrOperandsCollector {

	private final List<Expression> conditionalOrOperands = new ArrayList<>();

	static List<Expression> collectInfixOrOperands(Expression expression) {
		ConditionalOrOperandsCollector collector = new ConditionalOrOperandsCollector();
		collector.addExpression(expression);
		return collector.getConditionalOrOperands();
	}

	private ConditionalOrOperandsCollector() {

	}

	private void addExpression(Expression expression) {
		InfixExpression conditionalOrInfixExpression = toConditionalOrExpression(expression).orElse(null);
		if (conditionalOrInfixExpression != null) {
			addExpression(conditionalOrInfixExpression.getLeftOperand());
			addExpression(conditionalOrInfixExpression.getRightOperand());
			ASTNodeUtil.convertToTypedList(conditionalOrInfixExpression.extendedOperands(), Expression.class)
				.forEach(this::addExpression);
		} else {
			conditionalOrOperands.add(expression);
		}
	}

	private static Optional<InfixExpression> toConditionalOrExpression(Expression expression) {
		if (expression.getNodeType() != ASTNode.INFIX_EXPRESSION) {
			return Optional.empty();
		}
		InfixExpression infixExpression = (InfixExpression) expression;
		Operator operator = infixExpression.getOperator();
		if (operator != Operator.CONDITIONAL_OR) {
			return Optional.empty();
		}
		return Optional.of(infixExpression);
	}

	private List<Expression> getConditionalOrOperands() {
		return conditionalOrOperands;
	}

}
