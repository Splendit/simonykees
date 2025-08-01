package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.markers.common.RemoveNullCheckBeforeInstanceofEvent;
import eu.jsparrow.rules.common.util.OperatorUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * A visitor to remove null-checks in conjunction with
 * {@link InstanceofExpression}.
 * <p/>
 * The following code:
 * 
 * <pre>
 * <code>
 * 	boolean isUser = x != null && x instanceof User;
 * 	boolean isNotUser = y == null || !(y instanceof User);
 * </code>
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * <code>
 * 	boolean isUser = x instanceof User;
 * 	boolean isNotUser = !(y instanceof User);
 * </code>
 * </pre>
 * 
 * @since 3.8.0
 */
public class RemoveNullCheckBeforeInstanceofASTVisitor extends AbstractASTRewriteASTVisitor
		implements RemoveNullCheckBeforeInstanceofEvent {

	@Override
	public boolean visit(InstanceofExpression expression) {
		Expression leftInstanceOfOperand = expression.getLeftOperand();
		if (leftInstanceOfOperand.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName variableName = (SimpleName) leftInstanceOfOperand;

		if (InfixExpression.RIGHT_OPERAND_PROPERTY == expression.getLocationInParent()) {
			InfixExpression infixExpression = (InfixExpression) expression.getParent();
			if (analyzeInfixExpression(infixExpression, variableName, InfixExpression.Operator.CONDITIONAL_AND,
					InfixExpression.Operator.NOT_EQUALS)) {
				doReplace(infixExpression, expression);
			}
		} else {
			PrefixExpression negatedInstanceOf = findNegatedPrefixExpression(expression);
			if (negatedInstanceOf == null
					|| InfixExpression.RIGHT_OPERAND_PROPERTY != negatedInstanceOf.getLocationInParent()) {
				return true;
			}

			InfixExpression infixExpression = (InfixExpression) negatedInstanceOf.getParent();
			if (analyzeInfixExpression(infixExpression, variableName, InfixExpression.Operator.CONDITIONAL_OR,
					InfixExpression.Operator.EQUALS)) {
				doReplace(infixExpression, negatedInstanceOf);
			}
		}

		return true;
	}

	private boolean analyzeInfixExpression(InfixExpression infixExpression, SimpleName variableName,
			InfixExpression.Operator infixCondition, InfixExpression.Operator nullCheckOperator) {
		if (infixExpression.hasExtendedOperands()) {
			return false;
		}

		if (infixExpression.getOperator() != infixCondition) {
			return false;
		}

		return OperatorUtil.isNullCheck(variableName, infixExpression.getLeftOperand(), nullCheckOperator);
	}

	private void doReplace(InfixExpression infixExpression, Expression expression) {
		ASTNode newExpression = astRewrite.createCopyTarget(expression);
		astRewrite.replace(infixExpression, newExpression, null);
		onRewrite();
		addMarkerEvent(infixExpression.getLeftOperand(), infixExpression, expression);
	}

	private PrefixExpression findNegatedPrefixExpression(Expression expression) {
		if (ParenthesizedExpression.EXPRESSION_PROPERTY == expression.getLocationInParent()) {
			ParenthesizedExpression parenthesizedExpression = (ParenthesizedExpression) expression.getParent();
			return findNegatedPrefixExpression(parenthesizedExpression);
		}

		if (PrefixExpression.OPERAND_PROPERTY == expression.getLocationInParent()) {
			PrefixExpression prefixExpression = (PrefixExpression) expression.getParent();
			if (PrefixExpression.Operator.NOT == prefixExpression.getOperator()) {
				return prefixExpression;
			}
		}
		return null;
	}

}
