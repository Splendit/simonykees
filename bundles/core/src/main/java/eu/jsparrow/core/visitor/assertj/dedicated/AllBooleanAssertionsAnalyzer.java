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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

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
 *
 * @since 4.8.0
 */
public class AllBooleanAssertionsAnalyzer {

	private static final Map<Operator, Operator> INFIX_OPERATOR_NEGATIONS_MAP;
	private static final Map<Operator, String> PRIMITIVE_INFIX_TO_METHOD_NAME_MAP;
	private final AssertJAssertThatWithAssertionData normalizedDataForBooleanAssertion;

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

	private AllBooleanAssertionsAnalyzer(AssertJAssertThatWithAssertionData normalizedDataForBooleanAssertion) {
		this.normalizedDataForBooleanAssertion = normalizedDataForBooleanAssertion;
	}

	static Optional<AllBooleanAssertionsAnalyzer> conditionalInstance(
			AssertJAssertThatWithAssertionData assertThatWithAssertionData) {
		String assertionName = assertThatWithAssertionData.getAssertionName();

		if (!assertionName.equals(Constants.IS_TRUE) && !assertionName.equals(Constants.IS_FALSE)) { // $NON-NLS-1$
			return Optional.empty();
		}

		Expression assertThatArgument = assertThatWithAssertionData.getAssertThatArgument();
		NotOperandUnwrapper notOperandUnwrapper = new NotOperandUnwrapper(assertThatArgument);
		if (assertionName.equals(Constants.IS_FALSE) ^ notOperandUnwrapper.isNegationByNot()) {
			assertionName = Constants.IS_FALSE;
		} else {
			assertionName = Constants.IS_TRUE;
		}
		Expression unwrappedAssertThatArgument = notOperandUnwrapper.getUnwrappedOperand();
		AssertJAssertThatWithAssertionData normalizedData = new AssertJAssertThatWithAssertionData(
				unwrappedAssertThatArgument, assertionName);

		return Optional.of(new AllBooleanAssertionsAnalyzer(normalizedData));
	}

	/**
	 * Tries to find a replacement for a boolean assertion on an instanceof
	 * expression. For example, for
	 * <p>
	 * {@code assertThat(s instanceof String).isTrue();}
	 * <p>
	 * the replacement is
	 * <p>
	 * {@code assertThat(s).isInstanceOf(String.class);}
	 * 
	 */
	Optional<BooleanAssertionWithInstanceofAnalysisResult> findResultForInstanceofAsAssertThatArgument() {
		Expression unwrappedAssertThatArgument = normalizedDataForBooleanAssertion.getAssertThatArgument();

		String assertionName = normalizedDataForBooleanAssertion.getAssertionName();

		if (assertionName.equals(Constants.IS_FALSE)) {
			return Optional.empty();
		}

		if (unwrappedAssertThatArgument.getNodeType() != ASTNode.INSTANCEOF_EXPRESSION) {
			return Optional.empty();
		}

		InstanceofExpression instanceofExpression = (InstanceofExpression) unwrappedAssertThatArgument;

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

	/**
	 * Tries to find a replacement for a boolean assertion in connection with an
	 * infix expression or a method invocation.
	 * <p>
	 * For example, {@code assertThat(x == 10).isTrue();} <br>
	 * can be replaced by {@code assertThat(x).isEqualTo(10);}
	 * <p>
	 * <p>
	 * The following assertion with a method invocation like
	 * {@code assertThat(string.equals("Hello World!")).isTrue();} <br can be
	 * replaced by {@code assertThat(string).isEqualTo("Hello World!");}
	 * <p>
	 */
	Optional<AssertJAssertThatWithAssertionData> findResultForOtherAssertThatArgument() {
		Expression unwrappedAssertThatArgument = normalizedDataForBooleanAssertion.getAssertThatArgument();

		if (unwrappedAssertThatArgument.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			return analyzeBooleanAssertionWithInfixOperation((InfixExpression) unwrappedAssertThatArgument);
		}

		if (unwrappedAssertThatArgument.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return analyzeBooleanAssertionWithMethodInvocation((MethodInvocation) unwrappedAssertThatArgument);

		}
		return Optional.empty();
	}

	private Optional<AssertJAssertThatWithAssertionData> analyzeBooleanAssertionWithInfixOperation(
			InfixExpression infixExpressionAsAssertThatArgument) {
		Expression leftOperand = infixExpressionAsAssertThatArgument.getLeftOperand();
		Expression rightOperand = infixExpressionAsAssertThatArgument.getRightOperand();
		Operator infixOperator = infixExpressionAsAssertThatArgument.getOperator();

		String assertionMethodName = normalizedDataForBooleanAssertion.getAssertionName();

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
		} else if (infixOperator == EQUALS) {
			newAssertionMethodName = Constants.IS_SAME_AS;
		} else if (infixOperator == NOT_EQUALS) {
			newAssertionMethodName = Constants.IS_NOT_SAME_AS;
		}
		if (newAssertionMethodName == null) {
			return Optional.empty();
		}
		return Optional.of(new AssertJAssertThatWithAssertionData(leftOperand,
				newAssertionMethodName, rightOperand));
	}

	private Optional<AssertJAssertThatWithAssertionData> analyzeBooleanAssertionWithMethodInvocation(
			MethodInvocation invocationAsAssertThatArgument) {

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
			return analyzer.findDedicatedAssertJAssertionData(normalizedDataForBooleanAssertion, newAssertThatArgument,
					invocationAsAssertThatArgument,
					newAssertThatArgumentTypeBinding,
					assertThatArgumentMethodBinding);

		}
		return Optional.empty();
	}

}
