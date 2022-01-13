package eu.jsparrow.core.visitor.assertj.dedicated;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.core.visitor.assertj.SupportedAssertJAssertions;
import eu.jsparrow.core.visitor.junit.dedicated.NotOperandUnwrapper;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * This visitor looks for AssertJ assertions which can be replaced by more
 * specific, dedicated AssertJ assertions.
 * <p>
 * For example, the AssertJ assertion
 * 
 * <pre>
 * assertThat(string.equals("Hello World!")).isTrue();
 * </pre>
 * 
 * can be replaced by a single one like
 * 
 * <pre>
 * assertThat(string).isEqualTo("Hello World!");
 * </pre>
 * 
 * @since 4.7.0
 *
 */
public class UseDedicatedAssertJAssertionsASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String ASSERT_THAT = "assertThat";//$NON-NLS-1$
	static final String IS_FALSE = "isFalse"; //$NON-NLS-1$
	static final String IS_TRUE = "isTrue"; //$NON-NLS-1$
	private static final Map<Operator, Operator> INFIX_OPERATOR_NEGATIONS_MAP;

	private static final Map<Operator, String> PRIMITIVE_INFIX_TO_METHOD_NAME_MAP;

	static {
		Map<Operator, Operator> tmpOperatorNegationMap = new HashMap<>();

		tmpOperatorNegationMap.put(EQUALS, NOT_EQUALS);
		tmpOperatorNegationMap.put(NOT_EQUALS, EQUALS);
		tmpOperatorNegationMap.put(LESS, GREATER_EQUALS);
		tmpOperatorNegationMap.put(LESS_EQUALS, GREATER);
		tmpOperatorNegationMap.put(GREATER, LESS_EQUALS);
		tmpOperatorNegationMap.put(GREATER_EQUALS, LESS);
		INFIX_OPERATOR_NEGATIONS_MAP = Collections.unmodifiableMap(tmpOperatorNegationMap);

		Map<Operator, String> tmpMethodNameMap = new HashMap<>();
		tmpMethodNameMap.put(EQUALS, "isEqualTo"); //$NON-NLS-1$
		tmpMethodNameMap.put(NOT_EQUALS, "isNotEqualTo"); //$NON-NLS-1$
		tmpMethodNameMap.put(LESS, "isLessThan"); //$NON-NLS-1$
		tmpMethodNameMap.put(LESS_EQUALS, "isLessThanOrEqualTo"); //$NON-NLS-1$
		tmpMethodNameMap.put(GREATER, "isGreaterThan"); //$NON-NLS-1$
		tmpMethodNameMap.put(GREATER_EQUALS, "isGreaterThanOrEqualTo"); //$NON-NLS-1$
		PRIMITIVE_INFIX_TO_METHOD_NAME_MAP = Collections.unmodifiableMap(tmpMethodNameMap);

	}

	@Override
	public boolean visit(MethodInvocation node) {
		DedicatedAssertionData data = findDedicatedAssertionData(node).orElse(null);
		if (data != null) {
			MethodInvocationData newAssertionData = data.getNewAssertionData()
				.orElse(null);
			SimpleType instanceofRightOperand = data.getInstanceofRightOperand()
				.orElse(null);

			if (newAssertionData != null) {
				MethodInvocation newAssertion = CopyMethodInvocation
					.createNewMethodInvocation(newAssertionData, astRewrite);
				MethodInvocation newAssertThat = CopyMethodInvocation
					.createNewMethodInvocation(data.getNewAssertThatData(), astRewrite);
				newAssertion.setExpression(newAssertThat);
				astRewrite.replace(node, newAssertion, null);
				onRewrite();

			} else if (instanceofRightOperand != null) {
				MethodInvocation newAssertion = createIsInstanceofInvocation(instanceofRightOperand);
				MethodInvocation newAssertThat = CopyMethodInvocation
					.createNewMethodInvocation(data.getNewAssertThatData(), astRewrite);
				newAssertion.setExpression(newAssertThat);
				astRewrite.replace(node, newAssertion, null);
				onRewrite();
			}

		}
		return true;
	}

	private Optional<DedicatedAssertionData> findDedicatedAssertionData(MethodInvocation node) {

		String assertionName = node
			.getName()
			.getIdentifier();

		if (!assertionName.equals(IS_TRUE) && !assertionName.equals(IS_FALSE)) { // $NON-NLS-1$
			return Optional.empty();
		}

		if (node.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		MethodInvocation assertThatInvocation = findAssertThatInvocationAsExpression(node).orElse(null);
		if (assertThatInvocation == null) {
			return Optional.empty();
		}

		List<Expression> assertThatArguments = ASTNodeUtil.convertToTypedList(assertThatInvocation.arguments(),
				Expression.class);

		if (assertThatArguments.size() != 1) {
			return Optional.empty();
		}
		Expression assertThatArgument = assertThatArguments.get(0);
		NotOperandUnwrapper notOperandUnwrapper = new NotOperandUnwrapper(assertThatArgument);
		if (assertionName.equals(IS_FALSE) ^ notOperandUnwrapper.isNegationByNot()) {
			assertionName = IS_FALSE;
		} else {
			assertionName = IS_TRUE;
		}
		Expression unwrappedAssertThatArgument = notOperandUnwrapper.getUnwrappedOperand();

		if (unwrappedAssertThatArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation invocationAsAssertThatArgument = (MethodInvocation) unwrappedAssertThatArgument;
			return analyzeBooleanAssertionWithMethodInvocation(assertThatInvocation, invocationAsAssertThatArgument,
					assertionName);
		}

		if (unwrappedAssertThatArgument.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			return analyzeBooleanAssertionWithInfixOperation(assertThatInvocation,
					(InfixExpression) unwrappedAssertThatArgument, assertionName);

		}

		if (unwrappedAssertThatArgument.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION) {
			InstanceofExpression instanceofExpression = (InstanceofExpression) unwrappedAssertThatArgument;
			return analyzeBooleanAssertionWithInstanceofOperation(assertThatInvocation,
					instanceofExpression, assertionName);
		}
		return Optional.empty();

	}

	private Optional<MethodInvocation> findAssertThatInvocationAsExpression(MethodInvocation booleanAssertion) {
		Expression invocationExpression = booleanAssertion.getExpression();
		if (invocationExpression == null || invocationExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}
		MethodInvocation assumedAssertThatInvocation = (MethodInvocation) invocationExpression;
		if (!ASSERT_THAT.equals(assumedAssertThatInvocation.getName()
			.getIdentifier())) {
			return Optional.empty();
		}
		IMethodBinding assertThatMethodBinding = assumedAssertThatInvocation.resolveMethodBinding();

		if (assertThatMethodBinding == null ||
				!SupportedAssertJAssertions.isSupportedAssertionsType(assertThatMethodBinding.getDeclaringClass())) {
			return Optional.empty();
		}
		return Optional.of(assumedAssertThatInvocation);
	}

	private Optional<DedicatedAssertionData> analyzeBooleanAssertionWithMethodInvocation(MethodInvocation assertThat,
			MethodInvocation invocationAsAssertThatArgument, String assertionMethodName) {

		Expression newAssertThatArgument = invocationAsAssertThatArgument.getExpression();
		if (newAssertThatArgument == null) {
			return Optional.empty();
		}
		List<Expression> newAssertionArguments = ASTNodeUtil.convertToTypedList(
				invocationAsAssertThatArgument.arguments(),
				Expression.class);

		if (newAssertionArguments.size() > 1) {
			return Optional.empty();
		}

		ITypeBinding newAssertThatArgumentTypeBinding = newAssertThatArgument.resolveTypeBinding();
		if (newAssertThatArgumentTypeBinding == null) {
			return Optional.empty();
		}

		IMethodBinding assertThatArgumentMethodBinding = invocationAsAssertThatArgument.resolveMethodBinding();
		if (assertThatArgumentMethodBinding == null) {
			return Optional.empty();
		}
		String newAssertionName;
		if (assertionMethodName.equals(IS_FALSE)) {
			newAssertionName = BooleanAssertionOnInvocationAnalyzer
				.findNewAssertionNameForIsFalse(newAssertThatArgumentTypeBinding, assertThatArgumentMethodBinding)
				.orElse(null);
		} else {
			newAssertionName = BooleanAssertionOnInvocationAnalyzer
				.findNewAssertionNameForIsTrue(newAssertThatArgumentTypeBinding, assertThatArgumentMethodBinding)
				.orElse(null);
		}

		if (newAssertionName == null) {
			return Optional.empty();
		}
		MethodInvocationData assertThatData = createNewAssertThatData(assertThat, newAssertThatArgument);
		MethodInvocationData newAssertionData = new MethodInvocationData(newAssertionName);
		newAssertionData.setArguments(newAssertionArguments);
		DedicatedAssertionData dedicatedAssertionData = new DedicatedAssertionData(assertThatData, newAssertionData);

		return Optional.of(dedicatedAssertionData);
	}

	private Optional<DedicatedAssertionData> analyzeBooleanAssertionWithInfixOperation(MethodInvocation assertThat,
			InfixExpression infixExpressionAsAssertThatArgument, String assertionMethodName) {
		Expression leftOperand = infixExpressionAsAssertThatArgument.getLeftOperand();
		Expression rightOperand = infixExpressionAsAssertThatArgument.getRightOperand();
		Operator infixOperator = infixExpressionAsAssertThatArgument.getOperator();
		if (assertionMethodName.equals(IS_FALSE)) {
			infixOperator = INFIX_OPERATOR_NEGATIONS_MAP.get(infixOperator);
		}
		if (leftOperand.getNodeType() == ASTNode.NULL_LITERAL) {
			if (rightOperand.getNodeType() == ASTNode.NULL_LITERAL) {
				return Optional.empty();
			}
			return analyzeBooleanAssertionWithInfixOperationWithNullLiteral(assertThat, rightOperand, infixOperator);
		}

		if (rightOperand.getNodeType() == ASTNode.NULL_LITERAL) {
			return analyzeBooleanAssertionWithInfixOperationWithNullLiteral(assertThat, leftOperand, infixOperator);
		}

		ITypeBinding leftOperandType = leftOperand.resolveTypeBinding();
		ITypeBinding rightOperandType = rightOperand.resolveTypeBinding();
		if (leftOperandType == null || rightOperandType == null) {
			return Optional.empty();
		}

		if (!ClassRelationUtil.compareITypeBinding(leftOperandType, rightOperandType)) {
			return Optional.empty();
		}

		if (!BooleanAssertionOnInvocationAnalyzer.isSupportedForInfixOrInstanceOf(leftOperandType)) {
			return Optional.empty();
		}

		String newAssertionMethodName = null;
		if (rightOperandType.isPrimitive()) {
			newAssertionMethodName = PRIMITIVE_INFIX_TO_METHOD_NAME_MAP.get(infixOperator);
		} else if (infixOperator == EQUALS) {
			newAssertionMethodName = "isSameAs"; //$NON-NLS-1$
		} else if (infixOperator == NOT_EQUALS) {
			newAssertionMethodName = "isNotSameAs"; //$NON-NLS-1$
		}
		if (newAssertionMethodName == null) {
			return Optional.empty();
		}

		MethodInvocationData assertThatData = createNewAssertThatData(assertThat, leftOperand);
		MethodInvocationData newAssertionData = new MethodInvocationData(newAssertionMethodName);
		newAssertionData.setArguments(Arrays.asList(rightOperand));
		DedicatedAssertionData dedicatedAssertionData = new DedicatedAssertionData(assertThatData, newAssertionData);

		return Optional.of(dedicatedAssertionData);

	}

	private Optional<DedicatedAssertionData> analyzeBooleanAssertionWithInfixOperationWithNullLiteral(
			MethodInvocation assertThat, Expression newAssertThatArgument, Operator infixOperator) {
		ITypeBinding newAssertThatArgumentType = newAssertThatArgument.resolveTypeBinding();
		if (!BooleanAssertionOnInvocationAnalyzer.isSupportedForInfixOrInstanceOf(newAssertThatArgumentType)) {
			return Optional.empty();
		}

		if (infixOperator == EQUALS) {
			MethodInvocationData assertThatData = createNewAssertThatData(assertThat, newAssertThatArgument);
			MethodInvocationData newAssertionData = new MethodInvocationData("isNull"); //$NON-NLS-1$
			return Optional.of(new DedicatedAssertionData(assertThatData, newAssertionData));

		} else if (infixOperator == NOT_EQUALS) {
			MethodInvocationData assertThatData = createNewAssertThatData(assertThat, newAssertThatArgument);
			MethodInvocationData newAssertionData = new MethodInvocationData("isNotNull"); //$NON-NLS-1$
			return Optional.of(new DedicatedAssertionData(assertThatData, newAssertionData));
		}
		return Optional.empty();
	}

	private Optional<DedicatedAssertionData> analyzeBooleanAssertionWithInstanceofOperation(
			MethodInvocation assertThat, InstanceofExpression instanceofExpression, String assertionName) {
		if (assertionName.equals(IS_FALSE)) {
			return Optional.empty();
		}
		Expression leftOperand = instanceofExpression.getLeftOperand();
		Type rightOperand = instanceofExpression.getRightOperand();
		if (rightOperand.getNodeType() != ASTNode.SIMPLE_TYPE) {
			return Optional.empty();
		}
		SimpleType simpleType = (SimpleType) rightOperand;

		ITypeBinding leftOperandType = leftOperand.resolveTypeBinding();
		if (leftOperandType == null) {
			return Optional.empty();
		}
		if (!BooleanAssertionOnInvocationAnalyzer.isSupportedForInfixOrInstanceOf(leftOperandType)) {
			return Optional.empty();
		}

		MethodInvocationData assertThatData = createNewAssertThatData(assertThat, leftOperand);
		return Optional.of(new DedicatedAssertionData(assertThatData, simpleType));
	}

	private MethodInvocationData createNewAssertThatData(MethodInvocation assertThat,
			Expression newAssertThatArgument) {
		String newAssertThaIdentifier = assertThat.getName()
			.getIdentifier();
		MethodInvocationData newAssertThatData = new MethodInvocationData(newAssertThaIdentifier);
		newAssertThatData.setExpression(assertThat.getExpression());
		List<Expression> newAssertThatArguments = Arrays.asList(newAssertThatArgument);
		newAssertThatData.setArguments(newAssertThatArguments);
		return newAssertThatData;
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createIsInstanceofInvocation(SimpleType instanceofRightOperand) {
		AST ast = astRewrite.getAST();
		MethodInvocation newAssertion = ast.newMethodInvocation();
		newAssertion.setName(ast.newSimpleName("isInstanceOf")); //$NON-NLS-1$
		SimpleType simpleTypeCopy = (SimpleType) astRewrite.createCopyTarget(instanceofRightOperand);
		TypeLiteral newAssertionArgument = ast.newTypeLiteral();
		newAssertionArgument.setType(simpleTypeCopy);
		newAssertion.arguments()
			.add(newAssertionArgument);
		return newAssertion;
	}

}
