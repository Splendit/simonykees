package eu.jsparrow.core.visitor.junit.dedicated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Analyzes an invocation of {@code assertTrue(condition)} or
 * {@code assertFalse(condition)} to in order to decide whether the given
 * invocation can be transformed to a more specific one.
 * 
 * @since 3.31.0
 *
 */
public class BooleanAssertionAnalyzer {
	private static final List<String> FLOATING_POINT_PRIMITIVES = Collections
		.unmodifiableList(Arrays.asList("float", "double")); //$NON-NLS-1$ //$NON-NLS-2$

	static final String ORG_JUNIT_JUPITER_API_ASSERTIONS = "org.junit.jupiter.api.Assertions"; //$NON-NLS-1$
	static final String ORG_JUNIT_ASSERT = "org.junit.Assert"; //$NON-NLS-1$
	static final String ASSERT_SAME = "assertSame"; //$NON-NLS-1$
	static final String ASSERT_NOT_SAME = "assertNotSame"; //$NON-NLS-1$
	static final String ASSERT_NULL = "assertNull"; //$NON-NLS-1$
	static final String ASSERT_NOT_NULL = "assertNotNull"; //$NON-NLS-1$
	static final String ASSERT_EQUALS = "assertEquals"; //$NON-NLS-1$
	static final String ASSERT_NOT_EQUALS = "assertNotEquals"; //$NON-NLS-1$
	static final String ASSERT_TRUE = "assertTrue"; //$NON-NLS-1$
	static final String ASSERT_FALSE = "assertFalse"; //$NON-NLS-1$

	Optional<DedicatedAssertionsAnalysisResult> analyzeAssertInvocation(MethodInvocation methodInvocation) {

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Optional.empty();
		}

		String methodName = methodBinding.getName();
		boolean usingAssertFalse;
		if (methodName.equals(ASSERT_TRUE) || methodName.equals(ASSERT_FALSE)) {
			usingAssertFalse = methodName.equals(ASSERT_FALSE);
		} else {
			return Optional.empty();
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		boolean usingJUnitJupiter;
		if (ClassRelationUtil.isContentOfTypes(declaringClass,
				Arrays.asList(ORG_JUNIT_ASSERT, ORG_JUNIT_JUPITER_API_ASSERTIONS))) {
			usingJUnitJupiter = ClassRelationUtil.isContentOfType(declaringClass, ORG_JUNIT_JUPITER_API_ASSERTIONS);
		} else {
			return Optional.empty();
		}

		List<Expression> originalArguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(),
				Expression.class);

		return analyzeAssertionArguments(originalArguments, usingAssertFalse, usingJUnitJupiter);
	}

	private Optional<DedicatedAssertionsAnalysisResult> analyzeAssertionArguments(List<Expression> originalArguments,
			boolean usingAssertFalse,
			boolean usingJUnitJupiter) {

		Expression booleanArgument;
		if (originalArguments.size() == 1) {
			booleanArgument = originalArguments.get(0);
		} else if (originalArguments.size() == 2) {
			if (usingJUnitJupiter) {
				booleanArgument = originalArguments.get(0);
			} else {
				booleanArgument = originalArguments.get(1);
			}
		} else {
			return Optional.empty();
		}

		NotOperandUnwrapper notOperandUnwrapper = new NotOperandUnwrapper(booleanArgument);
		boolean negation = usingAssertFalse ^ notOperandUnwrapper.isNegationByNot();
		Expression unwrappedOperand = notOperandUnwrapper.getUnwrappedOperand();

		if (unwrappedOperand.getNodeType() == ASTNode.INFIX_EXPRESSION) {
			return analyzeInfixExpression((InfixExpression) unwrappedOperand, originalArguments, usingJUnitJupiter,
					negation);
		}

		if (unwrappedOperand.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation equalsInvocation = (MethodInvocation) unwrappedOperand;
			return analyzeEqualsInvocation(equalsInvocation, originalArguments, usingJUnitJupiter, negation);
		}

		return Optional.empty();
	}

	private Optional<DedicatedAssertionsAnalysisResult> analyzeEqualsInvocation(MethodInvocation equalsInvocation,
			List<Expression> originalArguments,
			boolean usingJUnitJupiter, boolean negation) {
		List<Expression> operands = extractOperandsFromEqualsInvocation(equalsInvocation);
		if (operands.size() == 2) {
			if (!usingJUnitJupiter && comparingPrimitiveWithBoxed(operands.get(0), operands.get(1))) {
				return Optional.empty();
			}
			String newMethodName = getNewMethodNameForEqualsComparison(negation, operands);
			DedicatedAssertionsAnalysisResult analysisResult = createDedicatedAssertionAnalysisResult(
					originalArguments, operands, newMethodName, usingJUnitJupiter);
			return Optional.of(analysisResult);
		}
		return Optional.empty();
	}

	private String getNewMethodNameForEqualsComparison(boolean negation, List<Expression> operands) {
		if (operands.stream()
			.map(Expression::resolveTypeBinding)
			.allMatch(ITypeBinding::isArray)) {
			if (negation) {
				return ASSERT_NOT_SAME;
			}
			return ASSERT_SAME;
		}
		if (negation) {
			return ASSERT_NOT_EQUALS;
		}
		return ASSERT_EQUALS;
	}

	private DedicatedAssertionsAnalysisResult createDedicatedAssertionAnalysisResult(List<Expression> originalArguments,
			List<Expression> operands, String newMethodName, boolean usingJUnitJupiter) {
		List<Expression> newArguments = collectExpressionsForNewArguments(operands, originalArguments,
				usingJUnitJupiter);
		String declaringClassQualifiedName = usingJUnitJupiter ? ORG_JUNIT_JUPITER_API_ASSERTIONS : ORG_JUNIT_ASSERT;
		return new DedicatedAssertionsAnalysisResult(declaringClassQualifiedName, newMethodName, newArguments);
	}

	private List<Expression> extractOperandsFromEqualsInvocation(MethodInvocation equalsInvocation) {
		IMethodBinding methodBinding = equalsInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Collections.emptyList();
		}

		String methodName = methodBinding.getName();
		if (!"equals".equals(methodName)) { //$NON-NLS-1$
			return Collections.emptyList();
		}

		Expression leftOperand = equalsInvocation.getExpression();
		if (leftOperand == null) {
			return Collections.emptyList();
		}
		List<Expression> equalsInvocationArguments = ASTNodeUtil.convertToTypedList(equalsInvocation.arguments(),
				Expression.class);
		if (equalsInvocationArguments.size() != 1) {
			return Collections.emptyList();
		}
		Expression rightOperand = equalsInvocationArguments.get(0);

		int modifiers = methodBinding.getModifiers();
		if (Modifier.isStatic(modifiers)) {
			return Collections.emptyList();
		}

		if (SwapOperands.swapOperands(leftOperand, rightOperand)) {
			return Arrays.asList(rightOperand, leftOperand);
		}
		return Arrays.asList(leftOperand, rightOperand);
	}

	private Optional<DedicatedAssertionsAnalysisResult> analyzeInfixExpression(InfixExpression infixExpression,
			List<Expression> originalArguments,
			boolean usingJUnitJupiter, boolean negation) {

		InfixExpression.Operator operator = infixExpression.getOperator();

		if (operator == InfixExpression.Operator.EQUALS || operator == InfixExpression.Operator.NOT_EQUALS) {
			negation = negation ^ (operator == InfixExpression.Operator.NOT_EQUALS);

			Expression leftOperand = infixExpression.getLeftOperand();
			Expression rightOperand = infixExpression.getRightOperand();
			List<Expression> operands;
			if (SwapOperands.swapOperands(leftOperand, rightOperand)) {
				operands = Arrays.asList(rightOperand, leftOperand);
			} else {
				operands = Arrays.asList(leftOperand, rightOperand);
			}

			List<ITypeBinding> operandTypes = operands
				.stream()
				.map(Expression::resolveTypeBinding)
				.collect(Collectors.toList());
			boolean comparingPrimitives = operandTypes.stream()
				.allMatch(ITypeBinding::isPrimitive);

			boolean anyFloat = operandTypes
				.stream()
				.anyMatch(typeBinding -> ClassRelationUtil.isContentOfTypes(typeBinding, FLOATING_POINT_PRIMITIVES));

			if (anyFloat) {
				return Optional.empty();

			}
			boolean anyPrimitive = operandTypes.stream()
				.anyMatch(ITypeBinding::isPrimitive);
			if (!comparingPrimitives && anyPrimitive) {
				return Optional.empty();
			}

			boolean comparingWithNull;
			if (leftOperand.getNodeType() == ASTNode.NULL_LITERAL) {
				comparingWithNull = true;
				operands = Arrays.asList(rightOperand);
			} else if (rightOperand.getNodeType() == ASTNode.NULL_LITERAL) {
				comparingWithNull = true;
				operands = Arrays.asList(leftOperand);
			} else {
				comparingWithNull = false;
			}
			String newMethodName = getNewMethodNameForInfixComparison(comparingPrimitives, comparingWithNull, negation);
			DedicatedAssertionsAnalysisResult analysisResult = createDedicatedAssertionAnalysisResult(originalArguments,
					operands, newMethodName, usingJUnitJupiter);
			return Optional.of(analysisResult);
		}
		return Optional.empty();

	}

	private boolean comparingPrimitiveWithBoxed(Expression leftOperand, Expression rightOperand) {
		boolean leftPrimitive = leftOperand.resolveTypeBinding()
			.isPrimitive();
		boolean rightPrimitive = rightOperand.resolveTypeBinding()
			.isPrimitive();
		return leftPrimitive != rightPrimitive;
	}

	private List<Expression> collectExpressionsForNewArguments(List<Expression> operands,
			List<Expression> originalArguments, boolean usingJUnitJupiter) {

		Expression messageArgument = null;

		if (originalArguments.size() > 1) {
			if (usingJUnitJupiter) {
				messageArgument = originalArguments.get(1);
			} else {
				messageArgument = originalArguments.get(0);
			}
		}
		List<Expression> newArguments = new ArrayList<>();
		if (usingJUnitJupiter) {
			newArguments.addAll(operands);
			if (messageArgument != null) {
				newArguments.add(messageArgument);
			}
		} else {
			if (messageArgument != null) {
				newArguments.add(messageArgument);
			}
			newArguments.addAll(operands);
		}
		return newArguments;

	}

	private String getNewMethodNameForInfixComparison(boolean comparingPrimitives, boolean comparingWithNull,
			boolean negation) {

		if (comparingPrimitives) {
			if (negation) {
				return ASSERT_NOT_EQUALS;
			}
			return ASSERT_EQUALS;
		}

		if (comparingWithNull) {
			if (negation) {
				return ASSERT_NOT_NULL;
			}
			return ASSERT_NULL;
		}

		if (negation) {
			return ASSERT_NOT_SAME;
		}
		return ASSERT_SAME;
	}
}
