package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.visitor.junit.dedicated.NotOperandUnwrapper;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper class to analyze all possible kinds of boolean assertions which may be
 * replaced by more specific dedicated assertions. For example:
 * <ul>
 * <li>assertions on instanceof expressions</li>
 * <li>assertions on infix expressions</li>
 * <li>assertions on method invocations</li>
 * </ul>
 * For more informations, see
 * {@link #analyzeBooleanAssertion(AssertJAssertThatWithAssertionData)}
 *
 * @since 4.8.0
 */
public class AllBooleanAssertionsAnalyzer {

	private static final Map<Operator, Operator> INFIX_OPERATOR_NEGATIONS_MAP;
	private static final Map<Operator, String> PRIMITIVE_INFIX_TO_METHOD_NAME_MAP;
	private AssertJAssertThatWithAssertionData analysisResult;
	private BooleanAssertionWithInstanceofAnalysisResult analysisResultForInstanceofExpression;

	static {
		Map<Operator, Operator> tmpOperatorNegationMap = new HashMap<>();

		tmpOperatorNegationMap.put(InfixExpression.Operator.EQUALS, InfixExpression.Operator.NOT_EQUALS);
		tmpOperatorNegationMap.put(InfixExpression.Operator.NOT_EQUALS, InfixExpression.Operator.EQUALS);
		tmpOperatorNegationMap.put(InfixExpression.Operator.LESS, InfixExpression.Operator.GREATER_EQUALS);
		tmpOperatorNegationMap.put(InfixExpression.Operator.LESS_EQUALS, InfixExpression.Operator.GREATER);
		tmpOperatorNegationMap.put(InfixExpression.Operator.GREATER, InfixExpression.Operator.LESS_EQUALS);
		tmpOperatorNegationMap.put(InfixExpression.Operator.GREATER_EQUALS, InfixExpression.Operator.LESS);
		INFIX_OPERATOR_NEGATIONS_MAP = Collections.unmodifiableMap(tmpOperatorNegationMap);

		Map<Operator, String> tmpMethodNameMap = new HashMap<>();
		tmpMethodNameMap.put(InfixExpression.Operator.EQUALS, "isEqualTo"); //$NON-NLS-1$
		tmpMethodNameMap.put(InfixExpression.Operator.NOT_EQUALS, "isNotEqualTo"); //$NON-NLS-1$
		tmpMethodNameMap.put(InfixExpression.Operator.LESS, "isLessThan"); //$NON-NLS-1$
		tmpMethodNameMap.put(InfixExpression.Operator.LESS_EQUALS, "isLessThanOrEqualTo"); //$NON-NLS-1$
		tmpMethodNameMap.put(InfixExpression.Operator.GREATER, "isGreaterThan"); //$NON-NLS-1$
		tmpMethodNameMap.put(InfixExpression.Operator.GREATER_EQUALS, "isGreaterThanOrEqualTo"); //$NON-NLS-1$
		PRIMITIVE_INFIX_TO_METHOD_NAME_MAP = Collections.unmodifiableMap(tmpMethodNameMap);

	}

	/**
	 * Tries to find a replacement for a boolean assertion.
	 * <p>
	 * For example, the following infix operation with <br>
	 * {@code assertThat(x == 10).isTrue();}<br>
	 * can be replaced by <br>
	 * {@code assertThat(x).isEqualTo(10);}
	 * <p>
	 * An assertion with a method invocation like<br>
	 * {@code assertThat(string.equals("Hello World!")).isTrue();}<br>
	 * can be replaced by<br>
	 * {@code assertThat(string).isEqualTo("Hello World!");}
	 * <p>
	 * An assertion with an instanceof expression like<br>
	 * {@code assertThat(s instanceof String).isTrue();}<br>
	 * can be replaced by<br>
	 * {@code assertThat(s).isInstanceOf(String.class);}
	 * 
	 */
	void analyzeBooleanAssertion(final AssertJAssertThatWithAssertionData originalData) {

		String booleanAssertionName = originalData.getAssertionName();

		if (booleanAssertionName.equals(Constants.IS_TRUE) || booleanAssertionName.equals(Constants.IS_FALSE)) {

			Expression assertThatArgument = originalData.getAssertThatArgument();
			NotOperandUnwrapper notOperandUnwrapper = new NotOperandUnwrapper(assertThatArgument);
			if (booleanAssertionName.equals(Constants.IS_FALSE) ^ notOperandUnwrapper.isNegationByNot()) {
				booleanAssertionName = Constants.IS_FALSE;
			} else {
				booleanAssertionName = Constants.IS_TRUE;
			}
			Expression unwrappedAssertThatArgument = notOperandUnwrapper.getUnwrappedOperand();

			if (unwrappedAssertThatArgument.getNodeType() == ASTNode.INFIX_EXPRESSION) {
				analysisResult = analyzeBooleanAssertionWithInfixOperation(
						(InfixExpression) unwrappedAssertThatArgument,
						booleanAssertionName).orElse(null);

			} else if (unwrappedAssertThatArgument.getNodeType() == ASTNode.INSTANCEOF_EXPRESSION) {
				analysisResultForInstanceofExpression = analyzeBooleanAssertionWithInstanceofExpression(
						(InstanceofExpression) unwrappedAssertThatArgument, booleanAssertionName).orElse(null);

			} else if (unwrappedAssertThatArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
				analysisResult = analyzeBooleanAssertionWithMethodInvocation(
						(MethodInvocation) unwrappedAssertThatArgument,
						booleanAssertionName).orElse(null);

			}
		}
	}

	private static Optional<AssertJAssertThatWithAssertionData> analyzeBooleanAssertionWithInfixOperation(
			InfixExpression infixExpressionAsAssertThatArgument, String assertionMethodName) {

		Expression leftOperand = infixExpressionAsAssertThatArgument.getLeftOperand();
		Expression rightOperand = infixExpressionAsAssertThatArgument.getRightOperand();
		Operator infixOperator = infixExpressionAsAssertThatArgument.getOperator();

		if (assertionMethodName.equals(Constants.IS_FALSE)) {
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
		} else if (infixOperator == InfixExpression.Operator.EQUALS) {
			newAssertionMethodName = Constants.IS_SAME_AS;
		} else if (infixOperator == InfixExpression.Operator.NOT_EQUALS) {
			newAssertionMethodName = Constants.IS_NOT_SAME_AS;
		}
		if (newAssertionMethodName == null) {
			return Optional.empty();
		}
		return Optional.of(new AssertJAssertThatWithAssertionData(leftOperand,
				newAssertionMethodName, rightOperand));
	}

	private static Optional<BooleanAssertionWithInstanceofAnalysisResult> analyzeBooleanAssertionWithInstanceofExpression(
			InstanceofExpression instanceofExpression, String assertionName) {
		if (assertionName.equals(Constants.IS_FALSE)) {
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
		if (!SupportedAssertJAssertThatArgumentTypes.isSupportedAssertThatArgumentType(leftOperandType)) {
			return Optional.empty();
		}
		return Optional.of(new BooleanAssertionWithInstanceofAnalysisResult(leftOperand, simpleType));
	}

	private static Optional<AssertJAssertThatWithAssertionData> analyzeBooleanAssertionWithMethodInvocation(
			MethodInvocation invocationAsAssertThatArgument, String booleanAssertionName) {

		Expression newAssertThatArgument = invocationAsAssertThatArgument.getExpression();
		if (newAssertThatArgument == null) {
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

		BooleanAssertionOnInvocationAnalyzer analyzer = BooleanAssertionOnInvocationAnalyzerFactory
			.findAnalyzer(newAssertThatArgumentTypeBinding)
			.orElse(null);
		if (analyzer != null) {
			return analyzer.findDedicatedAssertJAssertionData(booleanAssertionName, newAssertThatArgument,
					invocationAsAssertThatArgument, newAssertThatArgumentTypeBinding, assertThatArgumentMethodBinding);
		}
		return Optional.empty();
	}

	Optional<AssertJAssertThatWithAssertionData> getAnalysisResult() {
		return Optional.ofNullable(analysisResult);
	}

	Optional<BooleanAssertionWithInstanceofAnalysisResult> getAnalysisResultForInstanceofExpression() {
		return Optional.ofNullable(analysisResultForInstanceofExpression);
	}

}
