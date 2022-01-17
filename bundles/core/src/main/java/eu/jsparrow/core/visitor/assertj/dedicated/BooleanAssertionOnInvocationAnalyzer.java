package eu.jsparrow.core.visitor.assertj.dedicated;

import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_EQUAL_TO;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.IS_NOT_EQUAL_TO;
import static eu.jsparrow.core.visitor.assertj.dedicated.Constants.OBJECT_EQUALS;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * This helper class analyzes method invocations which are used as argument of
 * an AssertJ assertThat invocation in connection with the boolean assertion
 * {@code isTrue} or {@code isFalse} and provides the informations for a corresponding
 * dedicated assertion.
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

	private final Map<String, String> mapToAssertJAssertions;
	private final Map<String, String> mapToNegatedAssertJAssertions;
	private final Predicate<ITypeBinding> supportedTypeBindingPredicate;

	BooleanAssertionOnInvocationAnalyzer(Predicate<ITypeBinding> supportedTypeBindingPredicate,
			Map<String, String> mapToAssertJAssertions,
			Map<String, String> mapToNegatedAssertJAssertions) {
		this.supportedTypeBindingPredicate = supportedTypeBindingPredicate;

		Map<String, String> tmpMap = new HashMap<>();
		tmpMap.put(OBJECT_EQUALS, IS_EQUAL_TO);
		tmpMap.putAll(mapToAssertJAssertions);
		this.mapToAssertJAssertions = Collections.unmodifiableMap(tmpMap);

		tmpMap = new HashMap<>();
		tmpMap.put(OBJECT_EQUALS, IS_NOT_EQUAL_TO);
		tmpMap.putAll(mapToNegatedAssertJAssertions);
		this.mapToNegatedAssertJAssertions = Collections.unmodifiableMap(tmpMap);
	}

	Optional<MethodInvocationData> findDedicatedAssertJAssertionData(IMethodBinding methodBinding,
			List<Expression> newAssertionArguments, String booleanAssertion) {

		if (!analyzeMethodBinding(methodBinding)) {
			return Optional.empty();
		}

		String methodName = methodBinding.getName();
		String newAssertionName;
		if (booleanAssertion.equals(UseDedicatedAssertJAssertionsASTVisitor.IS_FALSE)) {
			newAssertionName = mapToNegatedAssertJAssertions.get(methodName);
		} else {
			newAssertionName = mapToAssertJAssertions.get(methodName);
		}
		if (newAssertionName != null) {
			MethodInvocationData dedicatedAssertionData = new MethodInvocationData(newAssertionName);
			dedicatedAssertionData.setArguments(newAssertionArguments);
			return Optional.of(dedicatedAssertionData);
		}
		return Optional.empty();

	}

	protected boolean analyzeMethodBinding(IMethodBinding methodBinding) {
		return analyzeEqualsMethodParameters(methodBinding);
	}

	private static boolean analyzeEqualsMethodParameters(IMethodBinding methodBinding) {
		String methodName = methodBinding.getName();
		if (!methodName.equals(OBJECT_EQUALS)) {
			return true;
		}
		ITypeBinding[] parameterTypes = methodBinding.getMethodDeclaration()
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
