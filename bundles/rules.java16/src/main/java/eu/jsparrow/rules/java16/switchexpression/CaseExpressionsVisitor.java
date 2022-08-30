package eu.jsparrow.rules.java16.switchexpression;

import static eu.jsparrow.rules.java16.switchexpression.IfExpressionAnalyzer.getLambdaForInfixExpressionToCaseExpression;
import static eu.jsparrow.rules.java16.switchexpression.IfExpressionAnalyzer.getLambdaForMethodInvocationToCaseExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
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
class CaseExpressionsVisitor extends ASTVisitor {
	private final List<Expression> caseExpressions = new ArrayList<>();
	private final Function<InfixExpression, Optional<Expression>> infixExpressionToCaseExpression;
	private final Function<MethodInvocation, Optional<Expression>> methodInvocationToCaseExpression;
	protected boolean unexpectedNode;

	CaseExpressionsVisitor(VariableForSwitchAnalysisData variableData) {
		infixExpressionToCaseExpression = getLambdaForInfixExpressionToCaseExpression(variableData);
		methodInvocationToCaseExpression = getLambdaForMethodInvocationToCaseExpression(variableData);
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
		Optional<Expression> optionalCaseExpression = infixExpressionToCaseExpression.apply(node);
		optionalCaseExpression.ifPresent(caseExpressions::add);
		unexpectedNode = optionalCaseExpression.isEmpty();
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		Optional<Expression> optionalCaseExpression = methodInvocationToCaseExpression.apply(node);
		optionalCaseExpression.ifPresent(caseExpressions::add);
		unexpectedNode = optionalCaseExpression.isEmpty();
		return false;

	}

	public List<Expression> getCaseExpressions() {
		return caseExpressions;
	}

}
