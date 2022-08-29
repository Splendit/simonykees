package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

	static final List<String> SUPPORTED_PRIMITIVE_TYPES = Arrays.asList(
			char.class, int.class, long.class)
		.stream()
		.map(Class::getName)
		.collect(Collectors.toUnmodifiableList());

	private IfExpressionAnalyzer() {
		// private default constructor in order to hide implicit public one.
	}

//	static Optional<VariableForSwitchAnalysisData> findVariableData(IfStatement ifStatement) {
//		FirstOperandVisitor visitor = new FirstOperandVisitor();
//		ifStatement.getExpression()
//			.accept(visitor);
//		InfixExpression firstInfixExpression = visitor.getFirstInfixExpression()
//			.orElse(null);
//		if (firstInfixExpression != null) {
//			return findVariableData(firstInfixExpression);
//		}
//		MethodInvocation firstMethodInvocation = visitor.getFirstMethodInvocation()
//			.orElse(null);
//		if (firstMethodInvocation != null) {
//			return findVariableData(firstMethodInvocation);
//		}
//
//		return Optional.empty();
//	}

//	static Optional<VariableForSwitchAnalysisData> findVariableData(InfixExpression infixExpression) {
//		if (!isEqualsInfixOperation(infixExpression)) {
//			return Optional.empty();
//		}
//		return findVariableDataForEqualsInfixExpression(
//				infixExpression.getLeftOperand(),
//				infixExpression.getRightOperand());
//
//	}

//	static Optional<VariableForSwitchAnalysisData> findVariableDataForEqualsInfixExpression(
//			Expression leftOperand, Expression rightOperand) {
//
//		ITypeBinding operandType = leftOperand.resolveTypeBinding();
//		if (!isTypeSupportedForInfixOperations(operandType)) {
//			return Optional.empty();
//		}
//
//		if (!ClassRelationUtil.compareITypeBinding(operandType, rightOperand.resolveTypeBinding())) {
//			return Optional.empty();
//		}
//
//		SimpleName variableName = findNameOfVariableForSwitch(leftOperand)
//			.orElse(findNameOfVariableForSwitch(rightOperand).orElse(null));
//
//		if (variableName == null) {
//			return Optional.empty();
//		}
//
//		return Optional.of(new VariableForSwitchAnalysisData(ASTNode.INFIX_EXPRESSION, variableName, operandType));
//
//	}

	static Optional<SimpleName> findNameOfVariableForSwitch(Expression operand) {
		if (operand.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		SimpleName simpleName = (SimpleName) operand;
		IBinding binding = simpleName.resolveBinding();
		if (binding == null) {
			return Optional.empty();
		}
		if (binding.getKind() != IBinding.VARIABLE) {
			return Optional.empty();
		}
		IVariableBinding variableBinding = (IVariableBinding) binding;
		if (variableBinding.isField()) {
			return Optional.empty();
		}
		return Optional.of(simpleName);
	}

//	static Optional<VariableForSwitchAnalysisData> findVariableData(MethodInvocation methodInvocation) {
//
//		List<Expression> equalsMethodInvocationOperands = findEqualsMethodInvocationOperands(methodInvocation);
//		if (equalsMethodInvocationOperands.size() != 2) {
//			return Optional.empty();
//		}
//		return findVariableDataForMethodInvocation(
//				equalsMethodInvocationOperands.get(0),
//				equalsMethodInvocationOperands.get(1));
//	}

//	static Optional<VariableForSwitchAnalysisData> findVariableDataForMethodInvocation(
//			Expression leftOperand, Expression rightOperand) {
//
//		ITypeBinding operandType = leftOperand.resolveTypeBinding();
//		if (!isString(operandType)) {
//			return Optional.empty();
//		}
//
//		if (!ClassRelationUtil.compareITypeBinding(operandType, rightOperand.resolveTypeBinding())) {
//			return Optional.empty();
//		}
//
//		SimpleName variableName = findNameOfVariableForSwitch(leftOperand)
//			.orElse(findNameOfVariableForSwitch(rightOperand).orElse(null));
//
//		if (variableName == null) {
//			return Optional.empty();
//		}
//
//		return Optional.of(new VariableForSwitchAnalysisData(ASTNode.METHOD_INVOCATION, variableName, operandType));
//	}

	static boolean isString(ITypeBinding typeBinding) {
		return ClassRelationUtil.isContentOfType(typeBinding, java.lang.String.class.getName());
	}

	static boolean isTypeSupportedForInfixOperations(ITypeBinding typeBinding) {
		return ClassRelationUtil.isContentOfTypes(typeBinding, SUPPORTED_PRIMITIVE_TYPES);
	}

//	static List<Expression> findCaseExpressions(VariableForSwitchAnalysisData variableData, IfStatement ifStatement) {
//		if (variableData.getOperationNodeType() == ASTNode.METHOD_INVOCATION) {
//			return findCaseExpressionsForEqualsMethod(variableData, ifStatement);
//		}
//		return findCaseExpressionsForEqualsInfix(variableData, ifStatement);
//	}

//	static List<Expression> findCaseExpressionsForEqualsInfix(VariableForSwitchAnalysisData variableData,
//			IfStatement ifStatement) {
//
//		InfixExpressionsCollectorVisitor collectorVisitor = new InfixExpressionsCollectorVisitor();
//		ifStatement.getExpression()
//			.accept(collectorVisitor);
//		List<InfixExpression> infixExpressions = collectorVisitor.getInfixExpressions();
//		List<Expression> caseExpressions = new ArrayList<>();
//		for (InfixExpression infixExpression : infixExpressions) {
//			Expression caseExpression = findCaseExpression(variableData, infixExpression).orElse(null);
//			if (caseExpression == null) {
//				return Collections.emptyList();
//			}
//			caseExpressions.add(caseExpression);
//		}
//		return caseExpressions;
//	}

//	static List<Expression> findCaseExpressionsForEqualsMethod(VariableForSwitchAnalysisData variableData,
//			IfStatement ifStatement) {
//
//		MethodInvocationsCollectorVisitor collectorVisitor = new MethodInvocationsCollectorVisitor();
//		ifStatement.getExpression()
//			.accept(collectorVisitor);
//		List<MethodInvocation> methodInvocations = collectorVisitor.getMethodInvocations();
//		List<Expression> caseExpressions = new ArrayList<>();
//		for (MethodInvocation methodInvocation : methodInvocations) {
//			Expression caseExpression = findCaseExpression(variableData, methodInvocation).orElse(null);
//			if (caseExpression == null) {
//				return Collections.emptyList();
//			}
//			caseExpressions.add(caseExpression);
//		}
//		return caseExpressions;
//
//	}

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

}
