package eu.jsparrow.rules.java16.switchexpression;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Helper visitor used to visit the expression of an if statement in order to
 * find a variable which may be used in connection with the transformation of an
 * if statement to a switch statement or to a switch expression.
 * <p>
 * Examples for operations where such a variable can be found:
 * <ul>
 * <li>Variable {@code x} in {@code x == 1}</li>
 * <li>Variable {@code s} in {@code "A".equals(s)}</li>
 * </ul>
 *
 */
abstract class AbstractIfExpressionVisitor extends ASTVisitor {

	protected boolean unexpectedNode;

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
		if (!analyzeInfixExpression(node)) {
			unexpectedNode = true;
		}
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (!analyzeMethodInvocation(node)) {
			unexpectedNode = true;
		}
		return false;
	}


	protected abstract boolean analyzeInfixExpression(InfixExpression node);

	protected abstract boolean analyzeMethodInvocation(MethodInvocation node);
}
