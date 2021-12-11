package eu.jsparrow.core.visitor.assertj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper class to find out whether an instance of {@link InvocationChainData}
 * represents a supported {@code assertThat} - invocation chain.
 * 
 * @see #hasSupportedAssertThatInvocation(InvocationChainData)
 * 
 * 
 * @since 4.6.0
 *
 */
class AssertThatInvocationChainAnalyzer {

	private static final List<String> SUPPORTED_ASSERT_THAT_METHODS = Collections.unmodifiableList(Arrays.asList(
			"assertThat", //$NON-NLS-1$
			"assertThatCode", //$NON-NLS-1$
			"assertThatThrownBy", //$NON-NLS-1$
			"assertThatObject"//$NON-NLS-1$
	));

	static Optional<FirstInvocationChainAnalysisResult> analyzeFirstInvocationChain(
			InvocationChainData invocationChainData) {
		MethodInvocation assumedAssertThatInvocation = invocationChainData.getLeftMostInvocation();
		String methodName = assumedAssertThatInvocation.getName()
			.getIdentifier();

		if (!SUPPORTED_ASSERT_THAT_METHODS.contains(methodName)) {
			return Optional.empty();
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(assumedAssertThatInvocation.arguments(),
				Expression.class);

		if (arguments.size() != 1) {
			return Optional.empty();
		}

		Expression argument = arguments.get(0);
		if (!isSupportedAssertThatArgumentStructure(argument)) {
			return Optional.empty();
		}

		IMethodBinding assumedAssertThatMethodBinding = assumedAssertThatInvocation.resolveMethodBinding();
		if (assumedAssertThatMethodBinding == null) {
			return Optional.empty();
		}

		ITypeBinding declaringClass = assumedAssertThatMethodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass,
				"org.assertj.core.api.Assertions") //$NON-NLS-1$
				&& !ClassRelationUtil.isContentOfType(declaringClass,
						"org.assertj.core.api.AssertionsForClassTypes") //$NON-NLS-1$
				&& !ClassRelationUtil.isContentOfType(declaringClass,
						"org.assertj.core.api.AssertionsForInterfaceTypes")) {//$NON-NLS-1$

			return Optional.empty();
		}

		if (!hasSupportedAssertionMethodNames(invocationChainData)) {
			return Optional.empty();
		}

		ITypeBinding assertThatReturnType = assumedAssertThatMethodBinding.getReturnType();
		List<ITypeBinding> assumedAssertionReturnTypes = findAssumedAssertionReturnTypes(invocationChainData);
		if (assumedAssertionReturnTypes.isEmpty()) {
			return Optional.empty();
		}

		ITypeBinding assumedFirstAssertionReturnType = assumedAssertionReturnTypes.get(0);
		List<ITypeBinding> assumedSubsequentAssertionReturnTypes = assumedAssertionReturnTypes.subList(1,
				assumedAssertionReturnTypes.size());

		if (!analyzeFirstAssertionReturnType(assertThatReturnType, assumedFirstAssertionReturnType)) {
			return Optional.empty();
		}

		if (!analyzeAssertionMethodReturnTypes(assumedFirstAssertionReturnType,
				assumedSubsequentAssertionReturnTypes)) {
			return Optional.empty();
		}

		return Optional.of(new FirstInvocationChainAnalysisResult(assumedAssertThatInvocation, assertThatReturnType,
				assumedFirstAssertionReturnType));

	}

	/**
	 * @return true if the leftmost invocation of a given instance of
	 *         {@link InvocationChainData} is a supported {@code assertThat} -
	 *         invocation, otherwise false.
	 */
	static boolean hasSupportedAssertThatInvocation(
			InvocationChainData invocationChainData) {

		MethodInvocation assumedAssertThatInvocation = invocationChainData.getLeftMostInvocation();
		String methodName = assumedAssertThatInvocation.getName()
			.getIdentifier();

		if (!SUPPORTED_ASSERT_THAT_METHODS.contains(methodName)) {
			return false;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(assumedAssertThatInvocation.arguments(),
				Expression.class);

		if (arguments.size() != 1) {
			return false;
		}

		Expression argument = arguments.get(0);
		if (!isSupportedAssertThatArgumentStructure(argument)) {
			return false;
		}

		IMethodBinding assumedAssertThatMethodBinding = assumedAssertThatInvocation.resolveMethodBinding();
		if (assumedAssertThatMethodBinding == null) {
			return false;
		}

		ITypeBinding declaringClass = assumedAssertThatMethodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass,
				"org.assertj.core.api.Assertions") //$NON-NLS-1$
				|| ClassRelationUtil.isContentOfType(declaringClass,
						"org.assertj.core.api.AssertionsForClassTypes") //$NON-NLS-1$
				|| ClassRelationUtil.isContentOfType(declaringClass,
						"org.assertj.core.api.AssertionsForInterfaceTypes");//$NON-NLS-1$
	}

	static boolean isSupportedAssertThatArgumentStructure(Expression assertThatArgument) {

		return assertThatArgument.getNodeType() == ASTNode.SIMPLE_NAME
				|| assertThatArgument.getNodeType() == ASTNode.QUALIFIED_NAME
				|| (assertThatArgument.getNodeType() == ASTNode.FIELD_ACCESS
						&& isSupportedFieldAccess((FieldAccess) assertThatArgument))
				|| assertThatArgument.getNodeType() == ASTNode.THIS_EXPRESSION
				|| assertThatArgument.getNodeType() == ASTNode.SUPER_FIELD_ACCESS
				|| assertThatArgument.getNodeType() == ASTNode.NUMBER_LITERAL
				|| assertThatArgument.getNodeType() == ASTNode.CHARACTER_LITERAL
				|| assertThatArgument.getNodeType() == ASTNode.STRING_LITERAL
				|| assertThatArgument.getNodeType() == ASTNode.TYPE_LITERAL;
	}

	private static boolean isSupportedFieldAccess(FieldAccess fieldAccess) {
		Expression fieldAccessExpression = fieldAccess.getExpression();

		if (fieldAccessExpression != null) {
			if (fieldAccessExpression.getNodeType() == ASTNode.FIELD_ACCESS) {
				return isSupportedFieldAccess((FieldAccess) fieldAccessExpression);
			}
			return fieldAccessExpression.getNodeType() == ASTNode.THIS_EXPRESSION ||
					fieldAccessExpression.getNodeType() == ASTNode.SUPER_FIELD_ACCESS;
		}
		return false;
	}

	/**
	 * @return true if the names of all assertions following the
	 *         {@code assertThat} - invocation are supported, otherwise false.
	 */
	static boolean hasSupportedAssertionMethodNames(InvocationChainData invocationChainData) {
		List<MethodInvocation> chainFollowingAssertThat = invocationChainData.getSubsequentInvocations();
		return chainFollowingAssertThat.stream()
			.map(MethodInvocation::getName)
			.map(SimpleName::getIdentifier)
			.allMatch(SupportedAssertJAssertions::isSupportedAssertJAssertionMethodName);
	}

	private static List<ITypeBinding> findAssumedAssertionReturnTypes(InvocationChainData invocationChainData) {
		List<IMethodBinding> assertionMethodBindings = invocationChainData.getSubsequentInvocations()
			.stream()
			.map(MethodInvocation::resolveMethodBinding)
			.collect(Collectors.toList());

		if (assertionMethodBindings.stream()
			.anyMatch(Objects::isNull)) {
			return Collections.emptyList();
		}
		return assertionMethodBindings.stream()
			.map(IMethodBinding::getReturnType)
			.collect(Collectors.toList());
	}

	static Optional<ITypeBinding> findFirstAssertionReturnType(ITypeBinding assertThatReturnType,
			MethodInvocation assumedFirstAssertion) {

		IMethodBinding assumedFirstAssertionMethodBinding = assumedFirstAssertion.resolveMethodBinding();
		if (assumedFirstAssertionMethodBinding == null) {
			return Optional.empty();
		}
		ITypeBinding assumedFirstAssertionReturnType = assumedFirstAssertionMethodBinding.getReturnType();

		ITypeBinding lhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(assertThatReturnType);
		ITypeBinding rhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(assumedFirstAssertionReturnType);
		if (!ClassRelationUtil.compareITypeBinding(lhsNonParameterizedTypeErasure, rhsNonParameterizedTypeErasure)) {
			return Optional.empty();
		}
		return Optional.of(assumedFirstAssertionReturnType);

	}

	private static boolean analyzeFirstAssertionReturnType(ITypeBinding assertThatReturnType,
			ITypeBinding assumedFirstAssertionReturnType) {

		ITypeBinding lhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(assertThatReturnType);
		ITypeBinding rhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(assumedFirstAssertionReturnType);
		return ClassRelationUtil.compareITypeBinding(lhsNonParameterizedTypeErasure, rhsNonParameterizedTypeErasure);

	}
	
	static boolean analyzeAssertionMethodReturnTypes(ITypeBinding expectedAssertionReturnType,
			List<ITypeBinding> assertionReturnTypes) {

		return assertionReturnTypes.stream()
			.allMatch(returnType -> ClassRelationUtil.compareITypeBinding(returnType, expectedAssertionReturnType));
	}



	private static boolean compareErasureTypeBinding(ITypeBinding firstTypeBinding, ITypeBinding secondTypeBinding) {
		if (null == firstTypeBinding || null == secondTypeBinding) {
			return false;
		}
		ITypeBinding lhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(firstTypeBinding);
		ITypeBinding rhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(secondTypeBinding);
		return ClassRelationUtil.compareITypeBinding(lhsNonParameterizedTypeErasure, rhsNonParameterizedTypeErasure);
	}

	private static ITypeBinding getNonParameterizedTypeErasure(ITypeBinding typeBinding) {
		ITypeBinding erasure = typeBinding.getErasure();
		while (erasure.isParameterizedType()) {
			erasure = erasure.getErasure();
		}
		return erasure;
	}

	static Optional<ITypeBinding> findSupportedAssertionReturnType(InvocationChainData invocationChainData) {

		List<MethodInvocation> subsequentInvocations = invocationChainData.getSubsequentInvocations();
		ITypeBinding firstReturnTypeBinding = null;
		for (int i = 0; i < subsequentInvocations.size(); i++) {
			MethodInvocation assertion = subsequentInvocations.get(i);
			ITypeBinding typeBinding = assertion.resolveTypeBinding();
			if (i == 0) {
				firstReturnTypeBinding = typeBinding;
				if (firstReturnTypeBinding == null) {
					return Optional.empty();
				}
				if (ClassRelationUtil.isContentOfType(firstReturnTypeBinding, "void")) { //$NON-NLS-1$
					return Optional.empty();
				}

			} else if (!ClassRelationUtil.compareITypeBinding(firstReturnTypeBinding, typeBinding)) {
				return Optional.empty();
			}
		}
		return Optional.of(firstReturnTypeBinding);
	}

	static boolean hasSupportedAssertions(ITypeBinding assertThatReturntype,
			InvocationChainData invocationChainData) {

		List<MethodInvocation> subsequentInvocations = invocationChainData.getSubsequentInvocations();
		ITypeBinding firstAssertionReturnType = null;
		for (int i = 0; i < subsequentInvocations.size(); i++) {
			MethodInvocation assertion = subsequentInvocations.get(i);
			if (!SupportedAssertJAssertions.isSupportedAssertJAssertionMethodName(assertion.getName()
				.getIdentifier())) {
				return false;
			}
			IMethodBinding assertionMethodBinding = assertion.resolveMethodBinding();
			if (assertionMethodBinding == null) {
				return false;
			}
			if (i == 0) {
				firstAssertionReturnType = assertionMethodBinding.getReturnType();
				if (!compareErasureTypeBinding(assertThatReturntype, firstAssertionReturnType)) {
					return false;
				}
			} else if (!ClassRelationUtil.compareITypeBinding(firstAssertionReturnType,
					assertionMethodBinding.getReturnType())) {
				return false;
			}
		}
		return true;
	}

	private AssertThatInvocationChainAnalyzer() {
		// private constructor to hide the implicit public one.
	}
}
