package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Child classes of this visitor are expected to visit the condition of an if
 * statement in connection with a potential transformation of the given if
 * statement to a switch expression or a switch statement.
 * <p>
 * This visitor class only tolerated the following kinds of nodes:
 * <ul>
 * <li>conditional OR infix expressions</li>
 * <li>equals infix expressions</li>
 * <li>method invocations with the name "equals", for example
 * {@code s.equals("ABC")}</li>
 * </ul>
 * 
 * @since 4.3.0
 */
abstract class AbstractIfExpressionVisitor extends ASTVisitor {
	protected boolean unexpectedNode;

	static boolean isValidEqualsInfixExpression(InfixExpression infixExpression) {
		if (infixExpression.hasExtendedOperands()) {
			return false;
		}
		Operator operator = infixExpression.getOperator();
		return operator == Operator.EQUALS;
	}

	static Optional<Expression> findEqualsMethodArgument(MethodInvocation methodInvocation) {
		if (!methodInvocation.getName()
			.getIdentifier()
			.equals("equals")) { //$NON-NLS-1$
			return Optional.empty();
		}
		return ASTNodeUtil.findSingleInvocationArgument(methodInvocation);
	}

	@Override
	public boolean preVisit2(ASTNode node) {

		if (unexpectedNode) {
			return false;
		}
		if (node.getNodeType() == ASTNode.METHOD_INVOCATION ||
				node.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			return true;
		}
		unexpectedNode = true;
		return false;
	}

	@Override
	public boolean visit(InfixExpression node) {
		if (node.getOperator() == Operator.CONDITIONAL_OR) {
			return true;
		}
		if (!isValidEqualsInfixExpression(node)
				|| !analyzeEqualsInfixOperands(node.getLeftOperand(), node.getRightOperand())) {
			unexpectedNode = true;
		}
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		Expression invocationExpression = node.getExpression();
		Expression equalsMethodArgument = null;
		if (invocationExpression != null) {
			equalsMethodArgument = findEqualsMethodArgument(node).orElse(null);
		}
		unexpectedNode = invocationExpression == null || equalsMethodArgument == null
				|| !analyzeEqualsMethodOperands(invocationExpression, equalsMethodArgument);
		return false;
	}

	protected abstract boolean analyzeEqualsInfixOperands(Expression leftOperand, Expression rightOperand);

	protected abstract boolean analyzeEqualsMethodOperands(Expression equalsInvocationExpression,
			Expression equalsInvocationArgument);

	public boolean isUnexpectedNode() {
		return unexpectedNode;
	}
}
