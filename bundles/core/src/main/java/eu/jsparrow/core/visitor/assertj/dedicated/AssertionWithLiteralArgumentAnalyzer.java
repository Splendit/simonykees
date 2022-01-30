package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NumberLiteral;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper class to analyze all possible kinds of assertions in connection with
 * special literals which may be transformed.
 * <p>
 * for example, the following example like {@code assertThat(x).isEqualTo(0);}
 * <br>
 * can be replaced by {@code assertThat(x).isZero();}
 * 
 * @since 4.8.0
 */
public class AssertionWithLiteralArgumentAnalyzer {

	@SuppressWarnings("nls")
	private static final List<String> ZERO_LITERAL_TOKENS = Collections.unmodifiableList(Arrays.asList(
			"0", "0L", "0l", "0F", "0f", "0.0F", "0.0f", "0.0"));

	private static final List<String> ASSERT_THAT_TYPES_FOR_INT_ZERO = Stream.of(
			int.class,
			long.class,
			float.class,
			double.class,
			Integer.class,
			Long.class,
			Float.class,
			Double.class,
			java.math.BigInteger.class)
		.map(Class::getName)
		.collect(Collectors.toList());

	private static final List<String> ASSERT_THAT_TYPES_FOR_FLOAT_ZERO = Stream.of(
			float.class,
			double.class,
			Float.class,
			Double.class)
		.map(Class::getName)
		.collect(Collectors.toList());

	private static final List<String> ASSERT_THAT_TYPES_FOR_DOUBLE_ZERO = Stream.of(
			double.class,
			Double.class)
		.map(Class::getName)
		.collect(Collectors.toList());

	private static Optional<String> findNameReplacementForNullLiteralArgument(String methodName, Expression argument) {
		if (argument.getNodeType() == ASTNode.NULL_LITERAL) {
			if (methodName.equals(Constants.IS_SAME_AS) || methodName.equals(Constants.IS_EQUAL_TO)) {
				return Optional.of(Constants.IS_NULL);
			}
			if (methodName.equals(Constants.IS_NOT_SAME_AS) || methodName.equals(Constants.IS_NOT_EQUAL_TO)) {
				return Optional.of(Constants.IS_NOT_NULL);
			}
		}
		return Optional.empty();
	}

	private static Optional<String> findNameReplacementForZeroLiteralArgument(Expression assertThatArgument,
			String methodName,
			Expression assertionArgument) {
		if (assertionArgument.getNodeType() == ASTNode.NUMBER_LITERAL) {
			NumberLiteral numberLiteral = (NumberLiteral) assertionArgument;
			String numericTooken = numberLiteral.getToken();
			if (ZERO_LITERAL_TOKENS.contains(numericTooken)) {
				if (isSupportedNumericAssertThatArgument(assertThatArgument, numberLiteral)) {

					if (methodName.equals(Constants.IS_EQUAL_TO)) {
						return Optional.of(Constants.IS_ZERO);
					}
					if (methodName.equals(Constants.IS_NOT_EQUAL_TO)) {
						return Optional.of(Constants.IS_NOT_ZERO);
					}
					if (methodName.equals(Constants.IS_GREATER_THAN)) {
						return Optional.of(Constants.IS_POSITIVE);
					}
					if (methodName.equals(Constants.IS_LESS_THAN)) {
						return Optional.of(Constants.IS_NEGATIVE);
					}
					if (methodName.equals(Constants.IS_LESS_THAN_OR_EQUAL_TO)) {
						return Optional.of(Constants.IS_NOT_POSITIVE);
					}
					if (methodName.equals(Constants.IS_GREATER_THAN_OR_EQUAL_TO)) {
						return Optional.of(Constants.IS_NOT_NEGATIVE);
					}
				}
				if (methodName.equals(Constants.HAS_SIZE) ||
						methodName.equals(Constants.HAS_SIZE_LESS_THAN_OR_EQUAL_TO)) {
					return Optional.of(Constants.IS_EMPTY);
				} else if (methodName.equals(Constants.HAS_SIZE_GREATER_THAN)) {
					return Optional.of(Constants.IS_NOT_EMPTY);
				}
			}
		}
		return Optional.empty();
	}

	private static boolean isSupportedNumericAssertThatArgument(Expression assertThatArgument,
			NumberLiteral numberLiteral) {
		ITypeBinding assertThatArgumentTypeBinding = assertThatArgument.resolveTypeBinding();
		ITypeBinding numberLiteralTypeBinding = numberLiteral.resolveTypeBinding();

		if (ClassRelationUtil.isContentOfType(numberLiteralTypeBinding, int.class.getName())
				|| ClassRelationUtil.isContentOfType(numberLiteralTypeBinding, long.class.getName())) {
			return ClassRelationUtil.isContentOfTypes(assertThatArgumentTypeBinding, ASSERT_THAT_TYPES_FOR_INT_ZERO);
		}

		if (ClassRelationUtil.isContentOfType(numberLiteralTypeBinding, float.class.getName())) {
			return ClassRelationUtil.isContentOfTypes(assertThatArgumentTypeBinding, ASSERT_THAT_TYPES_FOR_FLOAT_ZERO);
		}

		if (ClassRelationUtil.isContentOfType(numberLiteralTypeBinding, double.class.getName())) {
			return ClassRelationUtil.isContentOfTypes(assertThatArgumentTypeBinding, ASSERT_THAT_TYPES_FOR_DOUBLE_ZERO);
		}
		return false;
	}

	private static Optional<String> findNameForAssertionWithoutArgument(Expression assertThatArgument,
			String methodName,
			Expression assertionArgument) {
		Optional<String> optionalNameForAssertionWithoutArgument = findNameReplacementForNullLiteralArgument(methodName,
				assertionArgument);
		if (optionalNameForAssertionWithoutArgument.isPresent()) {
			return optionalNameForAssertionWithoutArgument;
		}

		optionalNameForAssertionWithoutArgument = findNameReplacementForZeroLiteralArgument(assertThatArgument,
				methodName, assertionArgument);
		if (optionalNameForAssertionWithoutArgument.isPresent()) {
			return optionalNameForAssertionWithoutArgument;
		}
		return Optional.empty();
	}

	static Optional<AssertJAssertThatWithAssertionData> findDataForAssertionWithLiteral(
			AssertJAssertThatWithAssertionData assertThatWithAssertionData) {
		Expression assertionArgument = assertThatWithAssertionData.getAssertionArgument()
			.orElse(null);
		if (assertionArgument == null) {
			return Optional.empty();
		}
		String assertionName = assertThatWithAssertionData.getAssertionName();
		Expression assertThatArgument = assertThatWithAssertionData.getAssertThatArgument();
		return findNameForAssertionWithoutArgument(assertThatArgument, assertionName, assertionArgument)
			.map(nameForAssertionWithoutArgument -> new AssertJAssertThatWithAssertionData(assertThatArgument,
					nameForAssertionWithoutArgument));

	}

	static boolean isZeroLiteralToken(Expression expression) {
		if (expression.getNodeType() != ASTNode.NUMBER_LITERAL) {
			return false;
		}
		NumberLiteral numberLiteral = (NumberLiteral) expression;
		String numericTooken = numberLiteral.getToken();
		return ZERO_LITERAL_TOKENS.contains(numericTooken);
	}

	private AssertionWithLiteralArgumentAnalyzer() {
		/*
		 * private default constructor hiding implicit public one
		 */
	}

}
