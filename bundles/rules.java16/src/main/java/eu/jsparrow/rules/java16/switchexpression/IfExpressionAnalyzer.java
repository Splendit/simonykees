package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class IfExpressionAnalyzer {

	static final List<String> TYPES_FOR_EQUALS_INFIX_EXPRESSION = Arrays.asList(
			char.class, int.class, long.class)
		.stream()
		.map(Class::getName)
		.collect(Collectors.toUnmodifiableList());

	static final List<String> TYPES_FOR_EQUALS_METHOD_INVOCATION = Collections
		.singletonList(java.lang.String.class.getName());

	private IfExpressionAnalyzer() {
		// private default constructor in order to hide implicit public one.
	}
	
	static Function<InfixExpression, Optional<Expression>> getLambdaForInfixExpressionToCaseExpression(
			VariableForSwitchAnalysisData variableData) {
		if (ClassRelationUtil.isContentOfTypes(variableData.getOperandType(),
				IfExpressionAnalyzer.TYPES_FOR_EQUALS_INFIX_EXPRESSION)) {
			return infixExpression -> IfExpressionAnalyzer.findCaseExpression(variableData, infixExpression);
		}
		return infixExpression -> Optional.empty();
	}

	static Function<MethodInvocation, Optional<Expression>> getLambdaForMethodInvocationToCaseExpression(
			VariableForSwitchAnalysisData variableData) {
		if (ClassRelationUtil.isContentOfTypes(variableData.getOperandType(),
				IfExpressionAnalyzer.TYPES_FOR_EQUALS_METHOD_INVOCATION)) {
			return methodInvocation -> IfExpressionAnalyzer.findCaseExpression(variableData, methodInvocation);
		}
		return methodInvocation -> Optional.empty();
	}

	static boolean isTypeSupportingEqualsMethodInvocation(ITypeBinding typeBinding) {
		return ClassRelationUtil.isContentOfType(typeBinding, java.lang.String.class.getName());
	}

	static boolean isTypeSupportingEqualsInfixExpression(ITypeBinding typeBinding) {
		return ClassRelationUtil.isContentOfTypes(typeBinding, TYPES_FOR_EQUALS_INFIX_EXPRESSION);
	}

	static Optional<Expression> findCaseExpression(VariableForSwitchAnalysisData variableData,
			InfixExpression infixExpression) {
		if (!isEqualsInfixOperation(infixExpression)) {
			return Optional.empty();
		}
		Expression leftOperand = infixExpression.getLeftOperand();
		Expression rightOperand = infixExpression.getRightOperand();
		return findDataForEqualsInfixExpression(variableData, leftOperand, rightOperand);
	}

	static Optional<Expression> findDataForEqualsInfixExpression(
			VariableForSwitchAnalysisData variableAnalysisData, Expression leftOperand, Expression rightOperand) {

		if (leftOperand.getNodeType() == ASTNode.SIMPLE_NAME && isNumericOrCharacterLiteral(rightOperand)) {
			return findEqualsOperationForSwitch(variableAnalysisData, (SimpleName) leftOperand, rightOperand);
		}

		if (rightOperand.getNodeType() == ASTNode.SIMPLE_NAME && isNumericOrCharacterLiteral(leftOperand)) {
			return findEqualsOperationForSwitch(variableAnalysisData, (SimpleName) rightOperand, leftOperand);
		}

		return Optional.empty();
	}

	static boolean isNumericOrCharacterLiteral(Expression expression) {
		return expression.getNodeType() == ASTNode.NUMBER_LITERAL
				|| expression.getNodeType() == ASTNode.CHARACTER_LITERAL;
	}

	static Optional<Expression> findEqualsOperationForSwitch(
			VariableForSwitchAnalysisData variableAnalysisData,
			SimpleName variableForSwitch, Expression literalExpression) {

		String expectedVariableIdentifier = variableAnalysisData.getVariableForSwitch()
			.getIdentifier();
		if (!variableForSwitch.getIdentifier()
			.equals(expectedVariableIdentifier)) {
			return Optional.empty();
		}
		IVariableBinding variableBinding = findSupportedVariableBinding(variableForSwitch).orElse(null);
		if (variableBinding == null) {
			return Optional.empty();
		}
		ITypeBinding variableType = variableBinding.getType();
		if (!ClassRelationUtil.compareITypeBinding(variableType, variableAnalysisData.getOperandType())) {
			return Optional.empty();
		}
		return Optional.of(literalExpression);

	}

	static Optional<IVariableBinding> findSupportedVariableBinding(SimpleName simpleName) {
		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return Optional.empty();
		}
		if (binding.getKind() != IBinding.VARIABLE) {
			return Optional.empty();
		}
		return Optional.of((IVariableBinding) binding)
			.filter(variableBinding -> !variableBinding.isField());
	}

	static Optional<Expression> findCaseExpression(VariableForSwitchAnalysisData variableData,
			MethodInvocation methodInvocation) {
		List<Expression> equalsMethodInvocationOperands = findEqualsMethodInvocationOperands(methodInvocation);
		if (equalsMethodInvocationOperands.size() != 2) {
			return Optional.empty();
		}
		Expression leftOperand = equalsMethodInvocationOperands.get(0);
		Expression rightOperand = equalsMethodInvocationOperands.get(1);
		return findDataForEqualsMethodInvocation(variableData, leftOperand, rightOperand);
	}

	static Optional<Expression> findDataForEqualsMethodInvocation(
			VariableForSwitchAnalysisData variableAnalysisData, Expression invocationExpression,
			Expression invocationArgument) {
		if (invocationExpression.getNodeType() == ASTNode.SIMPLE_NAME
				&& invocationArgument.getNodeType() == ASTNode.STRING_LITERAL) {
			return findEqualsOperationForSwitch(variableAnalysisData, (SimpleName) invocationExpression,
					invocationArgument);
		}

		if (invocationArgument.getNodeType() == ASTNode.SIMPLE_NAME
				&& invocationExpression.getNodeType() == ASTNode.STRING_LITERAL) {
			return findEqualsOperationForSwitch(variableAnalysisData, (SimpleName) invocationArgument,
					invocationExpression);
		}

		return Optional.empty();
	}

	private static boolean isEqualsInfixOperation(InfixExpression infixExpression) {
		if (infixExpression.hasExtendedOperands()) {
			return false;
		}
		Operator operator = infixExpression.getOperator();
		return operator == Operator.EQUALS;
	}

	private static List<Expression> findEqualsMethodInvocationOperands(MethodInvocation methodInvocation) {
		if (!methodInvocation.getName()
			.getIdentifier()
			.equals("equals")) {
			return Collections.emptyList();
		}
		Expression invocationExpression = methodInvocation.getExpression();
		if (invocationExpression == null) {
			return Collections.emptyList();
		}

		List<Expression> invocationArgumentList = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);
		if (invocationArgumentList.size() != 1) {
			return Collections.emptyList();
		}
		List<Expression> operands = new ArrayList<>();
		operands.add(invocationExpression);
		operands.add(invocationArgumentList.get(0));
		return operands;
	}

	static Optional<VariableForSwitchAnalysisData> findVariableAnalysisResult(InfixExpression infixExpression) {
		if (!isEqualsInfixOperation(infixExpression)) {
			return Optional.empty();
		}
		Expression leftOperand = infixExpression.getLeftOperand();
		Expression rightOperand = infixExpression.getRightOperand();
		return findVariableForSwitchAnalysisResult(leftOperand, rightOperand, TYPES_FOR_EQUALS_INFIX_EXPRESSION);
	}

	static Optional<VariableForSwitchAnalysisData> findVariableAnalysisResult(MethodInvocation methodInvocation) {
		List<Expression> operands = findEqualsMethodInvocationOperands(methodInvocation);
		if (operands.size() != 2) {
			return Optional.empty();
		}
		Expression leftOperand = operands.get(0);
		Expression rightOperand = operands.get(1);
		return findVariableForSwitchAnalysisResult(leftOperand, rightOperand, TYPES_FOR_EQUALS_METHOD_INVOCATION);
	}

	static Optional<VariableForSwitchAnalysisData> findVariableForSwitchAnalysisResult(Expression leftOperand,
			Expression rightOperand, List<String> supportedOperandTypes) {

		SimpleName simpleName;
		if (leftOperand.getNodeType() == ASTNode.SIMPLE_NAME) {
			simpleName = (SimpleName) leftOperand;
		} else if (rightOperand.getNodeType() == ASTNode.SIMPLE_NAME) {
			simpleName = (SimpleName) rightOperand;
		} else {
			return Optional.empty();
		}
		IVariableBinding variableBinding = IfExpressionAnalyzer.findSupportedVariableBinding(simpleName)
			.orElse(null);
		if (variableBinding == null) {
			return Optional.empty();
		}
		return Optional.of(variableBinding)
			.map(IVariableBinding::getType)
			.filter(typeBinding -> ClassRelationUtil.isContentOfTypes(typeBinding, supportedOperandTypes))
			.map(typeBinding -> new VariableForSwitchAnalysisData(simpleName, typeBinding));
	}
}
