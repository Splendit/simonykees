package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 *
 */
class EqualsOperationForSwitchVisitor extends ASTVisitor {
	private final List<EqualsOperationForSwitch> equalsOperations = new ArrayList<>();
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
		if (equalsOperation != null) {
			equalsOperations.add(equalsOperation);
		} else {
			unexpectedNode = true;
		}
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		EqualsOperationForSwitch equalsOperation = EqualsOperationForSwitch.findEqualityOperationForSwitch(node)
			.orElse(null);
		if (equalsOperation != null) {
			equalsOperations.add(equalsOperation);
		} else {
			unexpectedNode = true;
		}
		return false;
	}

	public List<EqualsOperationForSwitch> getEqualsOperations() {
		return equalsOperations;
	}
}
