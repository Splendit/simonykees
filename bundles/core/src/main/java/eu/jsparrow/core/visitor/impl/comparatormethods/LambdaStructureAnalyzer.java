package eu.jsparrow.core.visitor.impl.comparatormethods;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class LambdaStructureAnalyzer {
	private final List<String> lambdaParameterIdentifiers;

	private String comparisonKeyMethodName;
	private boolean isReversed;
	private Type explicitLeftLambdaParameterType;
	String leftParameterIdentifier;

	LambdaStructureAnalyzer(LambdaExpression lambda) {
		List<VariableDeclaration> lambdaParameters = ASTNodeUtil.convertToTypedList(lambda.parameters(),
				VariableDeclaration.class);

		lambdaParameterIdentifiers = lambdaParameters.stream()
			.map(VariableDeclaration::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toList());

		if (lambdaParameters.get(0)
			.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			explicitLeftLambdaParameterType = ((SingleVariableDeclaration) lambdaParameters.get(0)).getType();
		} else {
			explicitLeftLambdaParameterType = null;
		}

	}

	boolean analyze(LambdaExpression lambda) {

		ASTNode lambdaBody = lambda.getBody();
		if (lambdaBody.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation lambdaBodyMethodInvocation = (MethodInvocation) lambdaBody;
		Expression bodyMethodInvocationExpression = lambdaBodyMethodInvocation.getExpression();
		if (bodyMethodInvocationExpression == null) {
			return false;
		}
		List<Expression> bodyMethodInvocationArguments = ASTNodeUtil
			.convertToTypedList(lambdaBodyMethodInvocation.arguments(), Expression.class);

		if (bodyMethodInvocationArguments.size() != 1) {
			return false;
		}
		Expression bodyMethodInvocationArgument = bodyMethodInvocationArguments.get(0);
		Pair<String> pairOfIdentifiers = findPairOfIdentifiers(bodyMethodInvocationExpression,
				bodyMethodInvocationArgument);
		Pair<MethodInvocation> pairOfMethodInvocations;
		if (pairOfIdentifiers.isEmpty()) {
			pairOfMethodInvocations = findPairOfMethodInvocations(bodyMethodInvocationExpression,
					bodyMethodInvocationArgument);
			pairOfIdentifiers = findPairOfIdentifiersAsInvocationExpressions(pairOfMethodInvocations.getLeftHS(),
					pairOfMethodInvocations.getRightHS());
		} else {
			pairOfMethodInvocations = Pair.empty();
		}
		if (pairOfIdentifiers.isEmpty()) {
			return false;
		}

		if (lambdaParameterIdentifiers.size() != 2) {
			return false;
		}
		if (pairOfIdentifiers.getLeftHS()
			.equals(lambdaParameterIdentifiers.get(0))) {
			if (!pairOfIdentifiers.getRightHS()
				.equals(lambdaParameterIdentifiers.get(1))) {
				return false;
			}
		} else if (pairOfIdentifiers.getLeftHS()
			.equals(lambdaParameterIdentifiers.get(1))) {
			if (!pairOfIdentifiers.getRightHS()
				.equals(lambdaParameterIdentifiers.get(0))) {
				return false;
			}
			isReversed = true;
		} else {
			return false;
		}
		return true;

	}

	private Pair<MethodInvocation> findPairOfMethodInvocations(Expression left, Expression right) {
		if (left.getNodeType() == ASTNode.METHOD_INVOCATION && right.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return Pair.of((MethodInvocation) left, (MethodInvocation) right);
		}
		return Pair.empty();
	}

	private Pair<String> findPairOfIdentifiers(Expression left, Expression right) {
		if (left.getNodeType() == ASTNode.SIMPLE_NAME && right.getNodeType() == ASTNode.SIMPLE_NAME) {
			return Pair.of(((SimpleName) left).getIdentifier(), ((SimpleName) right).getIdentifier());
		}
		return Pair.empty();
	}

	private Pair<String> findPairOfIdentifiersAsInvocationExpressions(MethodInvocation left, MethodInvocation right) {
		if (left.getExpression() == null || right.getExpression() == null) {
			return Pair.empty();
		}
		return findPairOfIdentifiers(left.getExpression(), right.getExpression());
	}

	Optional<Type> getExplicitLambdaParameterType() {
		return Optional.ofNullable(explicitLeftLambdaParameterType);
	}

	boolean isReversedOrder() {
		return isReversed;
	}

	Optional<String> getComparisonKeyMethodName() {
		return Optional.ofNullable(comparisonKeyMethodName);
	}

}
