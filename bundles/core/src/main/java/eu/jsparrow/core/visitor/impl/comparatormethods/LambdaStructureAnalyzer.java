package eu.jsparrow.core.visitor.impl.comparatormethods;

import java.util.Arrays;
import java.util.Collections;
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
	private final Type explicitLambdaParameterType;

	private MethodInvocation lambdaBodyMethodInvocation;
	private Expression bodyMethodInvocationExpression;
	private Expression bodyMethodInvocationArgument;
	private MethodInvocation invocationLeftHS;
	private MethodInvocation invocationRightHS;
	private List<String> lambdaParameterIdentifiersInBody;

	LambdaStructureAnalyzer(LambdaExpression lambda) {
		List<VariableDeclaration> lambdaParameters = ASTNodeUtil.convertToTypedList(lambda.parameters(),
				VariableDeclaration.class);

		lambdaParameterIdentifiers = lambdaParameters.stream()
			.map(VariableDeclaration::getName)
			.map(SimpleName::getIdentifier)
			.collect(Collectors.toList());

		if (lambdaParameters.get(0)
			.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			explicitLambdaParameterType = ((SingleVariableDeclaration) lambdaParameters.get(0)).getType();
		} else {
			explicitLambdaParameterType = null;
		}

	}

	boolean analyze(LambdaExpression lambda) {
		if (lambdaParameterIdentifiers.size() != 2) {
			return false;
		}
		ASTNode lambdaBody = lambda.getBody();
		if (lambdaBody.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		lambdaBodyMethodInvocation = (MethodInvocation) lambdaBody;

		bodyMethodInvocationExpression = lambdaBodyMethodInvocation.getExpression();
		if (bodyMethodInvocationExpression == null) {
			return false;
		}
		List<Expression> bodyMethodInvocationArguments = ASTNodeUtil
			.convertToTypedList(lambdaBodyMethodInvocation.arguments(), Expression.class);

		if (bodyMethodInvocationArguments.size() != 1) {
			return false;
		}
		bodyMethodInvocationArgument = bodyMethodInvocationArguments.get(0);

		lambdaParameterIdentifiersInBody = extractParameterIdentifiersUsedInLambdaBody(bodyMethodInvocationExpression,
				bodyMethodInvocationArgument);
		if (lambdaParameterIdentifiersInBody.isEmpty()) {
			return false;
		}

		if (bodyMethodInvocationExpression.getNodeType() == ASTNode.METHOD_INVOCATION
				&& bodyMethodInvocationArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			invocationLeftHS = (MethodInvocation) bodyMethodInvocationExpression;
			invocationRightHS = (MethodInvocation) bodyMethodInvocationArgument;
		}

		return true;
	}

	private List<String> extractParameterIdentifiersUsedInLambdaBody(Expression bodyMethodInvocationExpression,
			Expression bodyMethodInvocationArgument) {

		String identidierLHS;
		String identidierRHS;
		if (bodyMethodInvocationExpression.getNodeType() == ASTNode.SIMPLE_NAME
				&& bodyMethodInvocationArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			identidierLHS = ((SimpleName) bodyMethodInvocationExpression).getIdentifier();
			identidierRHS = ((SimpleName) bodyMethodInvocationArgument).getIdentifier();

		} else if (bodyMethodInvocationExpression.getNodeType() == ASTNode.METHOD_INVOCATION
				&& bodyMethodInvocationArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			invocationLeftHS = (MethodInvocation) bodyMethodInvocationExpression;
			invocationRightHS = (MethodInvocation) bodyMethodInvocationArgument;
			if (invocationLeftHS.getExpression() == null || invocationLeftHS.getExpression()
				.getNodeType() != ASTNode.SIMPLE_NAME) {

			}
			identidierLHS = ((SimpleName) invocationLeftHS.getExpression()).getIdentifier();
			if (invocationRightHS.getExpression() == null || invocationRightHS.getExpression()
				.getNodeType() != ASTNode.SIMPLE_NAME) {

			}
			identidierRHS = ((SimpleName) invocationRightHS.getExpression()).getIdentifier();
		} else {
			return Collections.emptyList();
		}
		int indexOfLHS = lambdaParameterIdentifiers.indexOf(identidierLHS);
		int indexOfRHS = lambdaParameterIdentifiers.indexOf(identidierRHS);
		if (!(indexOfLHS == 0 && indexOfRHS == 1 || indexOfLHS == 1 && indexOfRHS == 0)) {
			return Arrays.asList(identidierLHS, identidierRHS);
		}
		return Collections.emptyList();

	}

	Optional<Type> getExplicitLambdaParameterType() {
		return Optional.ofNullable(explicitLambdaParameterType);
	}

	boolean isReversedOrder() {
		return lambdaParameterIdentifiersInBody.get(0)
			.equals(lambdaParameterIdentifiers.get(1));
	}

}
