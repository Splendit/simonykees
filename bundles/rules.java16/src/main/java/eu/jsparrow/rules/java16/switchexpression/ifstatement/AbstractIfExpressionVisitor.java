package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
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
		EqualsOperationForSwitch equalsOperation = EqualsOperationForSwitch.findEqualityOperationForSwitch(node)
			.orElse(null);
		unexpectedNode = equalsOperation == null || !analyzeEqualsOperationForSwitch(equalsOperation);
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		EqualsOperationForSwitch equalsOperation = EqualsOperationForSwitch.findEqualityOperationForSwitch(node)
			.orElse(null);
		unexpectedNode = equalsOperation == null || !analyzeEqualsOperationForSwitch(equalsOperation);
		return false;
	}

	protected abstract boolean analyzeEqualsOperationForSwitch(EqualsOperationForSwitch equalsOperation);

}
