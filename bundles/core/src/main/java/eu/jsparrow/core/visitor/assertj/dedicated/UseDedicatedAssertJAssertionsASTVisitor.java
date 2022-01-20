package eu.jsparrow.core.visitor.assertj.dedicated;

import static org.eclipse.jdt.core.dom.InfixExpression.Operator.EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.GREATER_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.LESS_EQUALS;
import static org.eclipse.jdt.core.dom.InfixExpression.Operator.NOT_EQUALS;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.core.visitor.junit.dedicated.NotOperandUnwrapper;
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

		AssertJAssertThatWithAssertionData assertThatWithAssertionData = AssertJAssertThatWithAssertionData
			.findDataForAssumedAssertion(node)
			.orElse(null);

		if (assertThatWithAssertionData == null) {
			return true;
		}

		AssertJAssertThatWithAssertionData dataForAssertionWithLiteral = AssertionWithLiteralArgumentAnalyzer
			.findDataForAssertionWithLiteral(assertThatWithAssertionData)
			.orElse(null);

		if (dataForAssertionWithLiteral != null) {
			transform(node, dataForAssertionWithLiteral);
			return true;
		}

		AssertJAssertThatWithAssertionData normalizedDataForBooleanAssertion = findNormalizedDataForBooleanAssertion(
				assertThatWithAssertionData).orElse(null);
		if (normalizedDataForBooleanAssertion != null) {

			Expression unwrappedAssertThatArgument = normalizedDataForBooleanAssertion.getAssertThatData()
				.getAssertThatArgument();

			if (unwrappedAssertThatArgument.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION) {
				BooleanAssertionWithInstanceofAnalyzer.findAssertThatInstanceOfAnalysisData(
						normalizedDataForBooleanAssertion, (InstanceofExpression) unwrappedAssertThatArgument)
					.ifPresent(data -> transform(node, data));
				return true;
			}

			if (unwrappedAssertThatArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
				analyzeBooleanAssertionWithMethodInvocation(normalizedDataForBooleanAssertion,
						(MethodInvocation) unwrappedAssertThatArgument)
							.map(this::replaceAssertionsWithLiteralArgument)
							.ifPresent(data -> transform(node, data));
				return true;
			}

			if (unwrappedAssertThatArgument.getNodeType() == ASTNode.INFIX_EXPRESSION) {
				analyzeBooleanAssertionWithInfixOperation(normalizedDataForBooleanAssertion,
						(InfixExpression) unwrappedAssertThatArgument)
							.map(this::replaceAssertionsWithLiteralArgument)
							.ifPresent(data -> transform(node, data));
				return true;
			}
		}
		return true;
	}

	private Optional<AssertJAssertThatWithAssertionData> findNormalizedDataForBooleanAssertion(
			AssertJAssertThatWithAssertionData assertThatWithAssertionData) {
		String assertionName = assertThatWithAssertionData.getAssertionName();

		if (!assertionName.equals(IS_TRUE) && !assertionName.equals(IS_FALSE)) { // $NON-NLS-1$
			return Optional.empty();
		}

		Expression assertThatArgument = assertThatWithAssertionData.getAssertThatData()
			.getAssertThatArgument();
		NotOperandUnwrapper notOperandUnwrapper = new NotOperandUnwrapper(assertThatArgument);
		if (assertionName.equals(IS_FALSE) ^ notOperandUnwrapper.isNegationByNot()) {
			assertionName = IS_FALSE;
		} else {
			assertionName = IS_TRUE;
		}
		Expression unwrappedAssertThatArgument = notOperandUnwrapper.getUnwrappedOperand();
		AssertJAssertThatWithAssertionData normalizedData = AssertJAssertThatWithAssertionData
			.createNewDataWithoutAssertionArgument(
					assertThatWithAssertionData, unwrappedAssertThatArgument, assertionName);

		return Optional.of(normalizedData);
	}

	private AssertJAssertThatWithAssertionData replaceAssertionsWithLiteralArgument(
			AssertJAssertThatWithAssertionData data) {
		return AssertionWithLiteralArgumentAnalyzer.findDataForAssertionWithLiteral(data)
			.orElse(data);
	}

	private static Optional<AssertJAssertThatWithAssertionData> analyzeBooleanAssertionWithMethodInvocation(
			AssertJAssertThatWithAssertionData assertThatWithAssertionData,
			MethodInvocation invocationAsAssertThatArgument) {

		Expression newAssertThatArgument = invocationAsAssertThatArgument.getExpression();
		if (newAssertThatArgument == null) {
			return Optional.empty();
		}

		ITypeBinding newAssertThatArgumentTypeBinding = newAssertThatArgument.resolveTypeBinding();
		if (newAssertThatArgumentTypeBinding == null) {
			return Optional.empty();
		}

		BooleanAssertionOnInvocationAnalyzer analyzer = BooleanAssertionOnInvocationAnalyzerFactory
			.findAnalyzer(newAssertThatArgumentTypeBinding)
			.orElse(null);
		if (analyzer != null) {
			return analyzer.findDedicatedAssertJAssertionData(assertThatWithAssertionData, newAssertThatArgument,
					invocationAsAssertThatArgument,
					newAssertThatArgumentTypeBinding);

		}
		return Optional.empty();
	}

	private Optional<AssertJAssertThatWithAssertionData> analyzeBooleanAssertionWithInfixOperation(
			AssertJAssertThatWithAssertionData normalizedDataForBooleanAssertion,
			InfixExpression infixExpressionAsAssertThatArgument) {
		Expression leftOperand = infixExpressionAsAssertThatArgument.getLeftOperand();
		Expression rightOperand = infixExpressionAsAssertThatArgument.getRightOperand();
		Operator infixOperator = infixExpressionAsAssertThatArgument.getOperator();

		String assertionMethodName = normalizedDataForBooleanAssertion.getAssertionName();

		if (assertionMethodName.equals(IS_FALSE)) {
			infixOperator = INFIX_OPERATOR_NEGATIONS_MAP.get(infixOperator);
		}

		if (leftOperand.getNodeType() == ASTNode.NULL_LITERAL) {
			if (rightOperand.getNodeType() == ASTNode.NULL_LITERAL) {
				return Optional.empty();
			}
			leftOperand = infixExpressionAsAssertThatArgument.getRightOperand();
			rightOperand = infixExpressionAsAssertThatArgument.getLeftOperand();
		}

		ITypeBinding leftOperandType = leftOperand.resolveTypeBinding();
		ITypeBinding rightOperandType = rightOperand.resolveTypeBinding();
		if (leftOperandType == null || rightOperandType == null) {
			return Optional.empty();
		}

		if (rightOperand.getNodeType() != ASTNode.NULL_LITERAL
				&& !ClassRelationUtil.compareITypeBinding(leftOperandType, rightOperandType)) {
			return Optional.empty();
		}

		if (!SupportedAssertJAssertThatArgumentTypes.isSupportedAssertThatArgumentType(leftOperandType)) {
			return Optional.empty();
		}

		String newAssertionMethodName = null;
		if (rightOperandType.isPrimitive()) {
			newAssertionMethodName = PRIMITIVE_INFIX_TO_METHOD_NAME_MAP.get(infixOperator);
		} else if (infixOperator == EQUALS) {
			newAssertionMethodName = Constants.IS_SAME_AS;
		} else if (infixOperator == NOT_EQUALS) {
			newAssertionMethodName = Constants.IS_NOT_SAME_AS;
		}
		if (newAssertionMethodName == null) {
			return Optional.empty();
		}
		return Optional.of(AssertJAssertThatWithAssertionData.createNewDataWithAssertionArgument(
				normalizedDataForBooleanAssertion, leftOperand, newAssertionMethodName, rightOperand));
	}

	private void transform(MethodInvocation node, BooleanAssertionWithInstanceofAnalysisResult data) {
		SimpleType instanceofRightOperand = data.getInstanceofRightOperand();
		MethodInvocation newAssertion = createIsInstanceofInvocation(instanceofRightOperand);
		AssertJAssertThatData newAssertThatData = data.getNewAssertThatData();
		MethodInvocation newAssertThatInvocation = createNewAssertThatInvocation(newAssertThatData);
		newAssertion.setExpression(newAssertThatInvocation);
		astRewrite.replace(node, newAssertion, null);
		onRewrite();
	}

	@SuppressWarnings("unchecked")
	private void transform(MethodInvocation node, AssertJAssertThatWithAssertionData data) {
		AST ast = astRewrite.getAST();
		MethodInvocation newAssertion = ast.newMethodInvocation();
		String newAssertionName = data.getAssertionName();
		newAssertion.setName(ast.newSimpleName(newAssertionName));
		data.getAssertionArgument()
			.map(assertionArgument -> (Expression) astRewrite.createCopyTarget(assertionArgument))
			.ifPresent(assertionArgument -> newAssertion.arguments()
				.add(assertionArgument));
		AssertJAssertThatData newAssertThatData = data.getAssertThatData();
		MethodInvocation newAssertThatInvocation = createNewAssertThatInvocation(newAssertThatData);
		newAssertion.setExpression(newAssertThatInvocation);
		astRewrite.replace(node, newAssertion, null);
		onRewrite();
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createNewAssertThatInvocation(AssertJAssertThatData newAssertThatData) {
		AST ast = astRewrite.getAST();
		MethodInvocation newAssertThatInvocation = ast.newMethodInvocation();
		newAssertThatInvocation.setName(ast.newSimpleName(newAssertThatData.getAssertThatMethodName()));
		Expression newAssertThatArgument = (Expression) astRewrite
			.createCopyTarget(newAssertThatData.getAssertThatArgument());
		newAssertThatInvocation.arguments()
			.add(newAssertThatArgument);
		newAssertThatData.getAssertThatInvocationExpression()
			.map(expression -> (Expression) astRewrite.createCopyTarget(expression))
			.ifPresent(newAssertThatInvocation::setExpression);
		return newAssertThatInvocation;
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
