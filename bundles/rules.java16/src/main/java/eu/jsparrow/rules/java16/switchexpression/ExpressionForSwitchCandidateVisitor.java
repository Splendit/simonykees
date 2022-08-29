package eu.jsparrow.rules.java16.switchexpression;

import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

public class ExpressionForSwitchCandidateVisitor extends ASTVisitor {

	private final IfStatement ifStatement;
	private VariableForSwitchAnalysisData variableData;

	boolean unexpectedNode;

	public ExpressionForSwitchCandidateVisitor(IfStatement ifStatement) {
		this.ifStatement = ifStatement;
	}

	@Override
	public boolean preVisit2(ASTNode node) {

		if (variableData != null) {
			return false;
		}

		if (unexpectedNode) {
			return false;
		}
		if (node.getNodeType() == ASTNode.METHOD_INVOCATION ||
				node.getNodeType() == ASTNode.INFIX_EXPRESSION ||
				node.getNodeType() == ASTNode.SIMPLE_NAME) {
			return true;
		}
		unexpectedNode = true;
		return false;
	}

	@Override
	public boolean visit(InfixExpression node) {
		if (analyzeExpressionParent(node)) {
			Operator operator = node.getOperator();
			return operator == Operator.CONDITIONAL_OR || operator == Operator.EQUALS;
		}
		unexpectedNode = true;
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (analyzeExpressionParent(node)) {
			String methodName = node.getName()
				.getIdentifier();
			if ("equals".equals(methodName)) {
				return true;
			}
		}
		unexpectedNode = true;
		return false;

	}

	@Override
	public boolean visit(SimpleName node) {
		Optional<VariableForSwitchAnalysisData> optionalData = findVariableDataForSwitch(node);
		optionalData.ifPresent(this::setVariableData);
		unexpectedNode = optionalData.isEmpty();
		return false;
	}

	private Optional<VariableForSwitchAnalysisData> findVariableDataForSwitch(SimpleName node) {
		ASTNode parent = node.getParent();
		Predicate<ITypeBinding> typeBindingPredicate;
		if (parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
			typeBindingPredicate = IfExpressionAnalyzer::isString;
		} else if (isInfixExpression(parent, Operator.EQUALS)) {
			typeBindingPredicate = IfExpressionAnalyzer::isTypeSupportedForInfixOperations;
		} else {
			return Optional.empty();
		}
		IVariableBinding variableBinding = IfExpressionAnalyzer.findSupportedVariableBinding(node)
			.orElse(null);
		if (variableBinding == null) {
			return Optional.empty();
		}
		return Optional.of(variableBinding)
			.map(IVariableBinding::getType)
			.filter(typeBindingPredicate)
			.map(typeBinding -> new VariableForSwitchAnalysisData(node, typeBinding));
	}

	private boolean analyzeExpressionParent(Expression expression) {
		ASTNode parent = expression.getParent();
		if (parent == ifStatement) {
			return true;
		}
		return isInfixExpression(parent, Operator.CONDITIONAL_OR);
	}

	private static boolean isInfixExpression(ASTNode node, Operator expectedOperator) {
		if (node.getNodeType() != ASTNode.INFIX_EXPRESSION) {
			return false;
		}
		InfixExpression parentInfixExpression = (InfixExpression) node;
		Operator parentInfixExpressionoperator = parentInfixExpression.getOperator();
		return parentInfixExpressionoperator == expectedOperator;
	}

	private void setVariableData(VariableForSwitchAnalysisData variableData) {
		this.variableData = variableData;
	}

	Optional<VariableForSwitchAnalysisData> getVariableData() {
		return Optional.ofNullable(variableData);
	}
}
