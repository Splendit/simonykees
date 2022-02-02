package eu.jsparrow.core.visitor.assertj.dedicated;

import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_EQUAL_TO;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_NOT_EQUAL_TO;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.OBJECT_EQUALS;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * This helper class analyzes method invocations which are used as argument of
 * an AssertJ assertThat invocation in connection with the boolean assertion
 * {@code isTrue} or {@code isFalse} and provides the informations for a
 * corresponding dedicated assertion.
 * 
 * For example:
 * <p>
 * For the AssertJ assertion
 * 
 * <pre>
 * assertThat(string.equals("Hello World!")).isTrue();
 * </pre>
 * 
 * the argument {@code string.equals("Hello World!")} is analyzed. The new
 * AssertJ assertThat argument will be {@code string} and the new dedicated
 * assertion will be {@code isEqualTo("Hello World!")}, resulting in
 * 
 * <pre>
 * assertThat(string).isEqualTo("Hello World!");
 * </pre>
 * 
 * @since 4.7.0
 * 
 */
class BooleanAssertionOnInvocationAnalyzer {

	private static final List<String> NUMBER_SINGLETON_LIST = Collections
		.singletonList(java.lang.Number.class.getName());
	@SuppressWarnings("nls")
	private static final List<String> NUMERIC_PRIMITIVE_LIST = Collections
		.unmodifiableList(Arrays.asList("byte", "short", "int", "long", "float", "double"));

	private final Map<String, String> mapToAssertJAssertions;
	private final Map<String, String> mapToNegatedAssertJAssertions;
	private final Predicate<ITypeBinding> supportedTypeBindingPredicate;

	BooleanAssertionOnInvocationAnalyzer(Predicate<ITypeBinding> supportedTypeBindingPredicate,
			Map<String, String> mapToAssertJAssertions,
			Map<String, String> mapToNegatedAssertJAssertions) {
		this.supportedTypeBindingPredicate = supportedTypeBindingPredicate;
		this.mapToAssertJAssertions = Collections.unmodifiableMap(mapToAssertJAssertions);
		this.mapToNegatedAssertJAssertions = Collections.unmodifiableMap(mapToNegatedAssertJAssertions);
	}

	Optional<AssertJAssertThatWithAssertionData> findDedicatedAssertJAssertionData(
			AssertJAssertThatWithAssertionData assertThatWithAssertionData,
			Expression newAssertThatArgument,
			MethodInvocation invocationAsAssertThatArgument,
			ITypeBinding newAssertThatArgumentTypeBinding,
			IMethodBinding assertThatArgumentMethodBinding) {

		List<Expression> newAssertionArguments = ASTNodeUtil.convertToTypedList(
				invocationAsAssertThatArgument.arguments(),
				Expression.class);

		if (newAssertionArguments.size() > 1) {
			return Optional.empty();
		}

		String booleanAssertion = assertThatWithAssertionData.getAssertionName();

		String methodNameToMap = invocationAsAssertThatArgument.getName()
			.getIdentifier();

		if (methodNameToMap.equals(Constants.OBJECT_EQUALS)) {
			return findDedicatedAssertJAssertionDataForEquals(booleanAssertion, newAssertThatArgument,
					newAssertionArguments, newAssertThatArgumentTypeBinding, assertThatArgumentMethodBinding);
		}

		String newAssertionName;
		if (booleanAssertion.equals(Constants.IS_FALSE)) {
			newAssertionName = mapToNegatedAssertJAssertions.get(methodNameToMap);
		} else {
			newAssertionName = mapToAssertJAssertions.get(methodNameToMap);
		}
		if (newAssertionName != null) {
			if (newAssertionArguments.isEmpty()) {

				return Optional.of(new AssertJAssertThatWithAssertionData(newAssertThatArgument, newAssertionName));
			}

			return Optional
				.of(new AssertJAssertThatWithAssertionData(newAssertThatArgument, newAssertionName,
						newAssertionArguments.get(0)));

		}
		return Optional.empty();

	}

	private Optional<AssertJAssertThatWithAssertionData> findDedicatedAssertJAssertionDataForEquals(
			String booleanAssertion, Expression newAssertThatArgument, List<Expression> newAssertionArguments,
			ITypeBinding newAssertThatArgumentTypeBinding, IMethodBinding equalsMethodBinding) {

		if (!analyzeEqualsMethodParameters(equalsMethodBinding)) {
			return Optional.empty();
		}
		if (newAssertionArguments.size() != 1) {
			return Optional.empty();
		}
		Expression equalsArgument = newAssertionArguments.get(0);

		if (booleanAssertion.equals(Constants.IS_TRUE) && equalsArgument.getNodeType() == ASTNode.NULL_LITERAL) {
			return Optional.empty();
		}

		ITypeBinding equalsArgumentTypeBinding = equalsArgument.resolveTypeBinding();
		if (equalsArgumentTypeBinding == null) {
			return Optional.empty();
		}
		if (isNumericEqualsDissimilarPrimitiveNumeric(newAssertThatArgumentTypeBinding, equalsArgumentTypeBinding)) {
			return Optional.empty();
		}
		String newAssertionName;
		if (SupportedAssertJAssertThatArgumentTypes.IS_SUPPORTED_ARRAY_TYPE.test(newAssertThatArgumentTypeBinding)) {
			if (booleanAssertion.equals(Constants.IS_FALSE)) {
				newAssertionName = Constants.IS_NOT_SAME_AS;
			} else {
				newAssertionName = Constants.IS_SAME_AS;
			}
		} else {
			if (booleanAssertion.equals(Constants.IS_FALSE)) {
				newAssertionName = Constants.IS_NOT_EQUAL_TO;
			} else {
				newAssertionName = Constants.IS_EQUAL_TO;
			}
		}
		return Optional
			.of(new AssertJAssertThatWithAssertionData(newAssertThatArgument, newAssertionName, equalsArgument));

	}

	private boolean isNumericEqualsDissimilarPrimitiveNumeric(ITypeBinding equalsExpressionTypeBinding,
			ITypeBinding equalsArgumentTypeBinding) {

		if (!ClassRelationUtil.isContentOfTypes(equalsArgumentTypeBinding, NUMERIC_PRIMITIVE_LIST)) {
			return false;
		}

		if (!ClassRelationUtil.isInheritingContentOfTypes(equalsExpressionTypeBinding,
				NUMBER_SINGLETON_LIST)) {
			return false;
		}

		return !ClassRelationUtil.compareBoxedITypeBinding(new ITypeBinding[] { equalsExpressionTypeBinding },
				new ITypeBinding[] { equalsArgumentTypeBinding });
	}

	private static boolean analyzeEqualsMethodParameters(IMethodBinding equalsMethodBinding) {
		ITypeBinding[] parameterTypes = equalsMethodBinding.getMethodDeclaration()
			.getParameterTypes();
		if (parameterTypes.length != 1) {
			return false;
		}
		return ClassRelationUtil.isContentOfType(parameterTypes[0], Object.class.getName());
	}

	boolean isSupportedForType(ITypeBinding typeBinding) {
		return supportedTypeBindingPredicate.test(typeBinding);
	}
}
