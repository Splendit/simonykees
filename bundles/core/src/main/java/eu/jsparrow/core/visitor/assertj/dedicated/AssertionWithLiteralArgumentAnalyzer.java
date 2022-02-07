package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
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

	private static final List<String> ASSERT_THAT_TYPES_FOR_BOOLEAN_LITERAL = Stream.of(boolean.class, Boolean.class)
		.map(Class::getName)
		.collect(Collectors.toList());

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

	private static final List<String> PRIMITIVE_INT_OR_PRIMITIVE_LONG = Stream.of(
			int.class,
			long.class)
		.map(Class::getName)
		.collect(Collectors.toList());

	private static final Map<String, String> RELATIONAL_METHOD_WITH_ZERO_LITERAL_MAP;

	static {
		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put(Constants.IS_EQUAL_TO, Constants.IS_ZERO);
		tmpMap.put(Constants.IS_NOT_EQUAL_TO, Constants.IS_NOT_ZERO);
		tmpMap.put(Constants.IS_GREATER_THAN, Constants.IS_POSITIVE);
		tmpMap.put(Constants.IS_LESS_THAN, Constants.IS_NEGATIVE);
		tmpMap.put(Constants.IS_LESS_THAN_OR_EQUAL_TO, Constants.IS_NOT_POSITIVE);
		tmpMap.put(Constants.IS_GREATER_THAN_OR_EQUAL_TO, Constants.IS_NOT_NEGATIVE);
		RELATIONAL_METHOD_WITH_ZERO_LITERAL_MAP = Collections.unmodifiableMap(tmpMap);
	}

	private AssertionWithLiteralArgumentAnalyzer() {
		/*
		 * private default constructor hiding implicit public one
		 */
	}

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
			String methodName, ITypeBinding zeroLiteralTypeBinding) {
		if (isSupportedNumericAssertThatArgument(assertThatArgument, zeroLiteralTypeBinding)) {
			return Optional.ofNullable(RELATIONAL_METHOD_WITH_ZERO_LITERAL_MAP.get(methodName));
		}
		if (methodName.equals(Constants.HAS_SIZE) ||
				methodName.equals(Constants.HAS_SIZE_LESS_THAN_OR_EQUAL_TO)) {
			return Optional.of(Constants.IS_EMPTY);
		}
		if (methodName.equals(Constants.HAS_SIZE_GREATER_THAN)) {
			return Optional.of(Constants.IS_NOT_EMPTY);
		}

		return Optional.empty();
	}

	private static boolean isSupportedNumericAssertThatArgument(Expression assertThatArgument,
			ITypeBinding zeroLiteralTypeBinding) {
		ITypeBinding assertThatArgumentTypeBinding = assertThatArgument.resolveTypeBinding();

		if (ClassRelationUtil.isContentOfTypes(zeroLiteralTypeBinding, PRIMITIVE_INT_OR_PRIMITIVE_LONG)) {
			return ClassRelationUtil.isContentOfTypes(assertThatArgumentTypeBinding, ASSERT_THAT_TYPES_FOR_INT_ZERO);
		}

		if (ClassRelationUtil.isContentOfType(zeroLiteralTypeBinding, float.class.getName())) {
			return ClassRelationUtil.isContentOfTypes(assertThatArgumentTypeBinding, ASSERT_THAT_TYPES_FOR_FLOAT_ZERO);
		}

		if (ClassRelationUtil.isContentOfType(zeroLiteralTypeBinding, double.class.getName())) {
			return ClassRelationUtil.isContentOfTypes(assertThatArgumentTypeBinding, ASSERT_THAT_TYPES_FOR_DOUBLE_ZERO);
		}
		return false;
	}

	static Optional<AssertJAssertThatWithAssertionData> findDataForAssertionWithBooleanLiteral(
			AssertJAssertThatWithAssertionData assertThatWithAssertionData) {
		Expression sameAssertThatArgument = assertThatWithAssertionData.getAssertThatArgument();
		if (!ClassRelationUtil.isContentOfTypes(sameAssertThatArgument.resolveTypeBinding(),
				ASSERT_THAT_TYPES_FOR_BOOLEAN_LITERAL)) {
			return Optional.empty();
		}

		Expression assertionArgument = assertThatWithAssertionData.getAssertionArgument()
			.orElse(null);
		if (assertionArgument == null) {
			return Optional.empty();
		}
		if (assertionArgument.getNodeType() != ASTNode.BOOLEAN_LITERAL) {
			return Optional.empty();
		}
		BooleanLiteral booleanLiteral = (BooleanLiteral) assertionArgument;
		String originalAssertionNamne = assertThatWithAssertionData.getAssertionName();
		String newAssertionName = null;

		boolean booleanValue = booleanLiteral.booleanValue();
		if (originalAssertionNamne.equals(Constants.IS_EQUAL_TO)
				|| originalAssertionNamne.equals(Constants.IS_SAME_AS)) {
			newAssertionName = booleanValue ? Constants.IS_TRUE : Constants.IS_FALSE;
		} else if (originalAssertionNamne.equals(Constants.IS_NOT_EQUAL_TO)
				|| originalAssertionNamne.equals(Constants.IS_NOT_SAME_AS)) {
			newAssertionName = booleanValue ? Constants.IS_FALSE : Constants.IS_TRUE;
		}

		if (newAssertionName == null) {
			return Optional.empty();
		}
		return Optional.of(new AssertJAssertThatWithAssertionData(sameAssertThatArgument, newAssertionName));
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

		if (isZeroLiteralToken(assertionArgument)) {
			ITypeBinding zeroLiteralTypeBinding = assertionArgument.resolveTypeBinding();
			return findNameReplacementForZeroLiteralArgument(assertThatArgument, assertionName, zeroLiteralTypeBinding)
				.map(nameForAssertionWithoutArgument -> new AssertJAssertThatWithAssertionData(
						assertThatArgument,
						nameForAssertionWithoutArgument));
		}

		return findNameReplacementForNullLiteralArgument(assertionName, assertionArgument)
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
}
