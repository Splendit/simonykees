package eu.jsparrow.core.visitor.assertj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTMatcher;
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
 * Helper class to analyze instances of {@link InvocationChainData} in order to
 * find out whether or not two or more invocation chains beginning with an
 * {@code assertThat} - invocation can be chained together to one single
 * invocation chain.
 * 
 * @since 4.6.0
 *
 */
class AssertThatInvocationChainAnalyzer {

	private static final ASTMatcher astMatcher = new ASTMatcher();
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

		return Optional
			.of(new FirstInvocationChainAnalysisResult(assumedAssertThatInvocation, assumedFirstAssertionReturnType));

	}

	private static boolean isSupportedAssertThatArgumentStructure(Expression assertThatArgument) {

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

	static boolean analyzeSubsequentInvocationChain(FirstInvocationChainAnalysisResult firstChainAnalysisResult,
			InvocationChainData invocationChainData) {
		MethodInvocation assertThatInvocation = firstChainAnalysisResult.getAssertThatInvocation();
		if (!astMatcher.match(assertThatInvocation, invocationChainData.getLeftMostInvocation())) {
			return false;
		}
		if (!hasSupportedAssertionMethodNames(invocationChainData)) {
			return false;
		}
		ITypeBinding firstAssertionReturnType = firstChainAnalysisResult.getFirstAssertionReturnType();
		List<ITypeBinding> assumedAssertionReturnTypes = findAssumedAssertionReturnTypes(invocationChainData);
		if (assumedAssertionReturnTypes.isEmpty()) {
			return false;
		}
		if (!analyzeAssertionMethodReturnTypes(firstAssertionReturnType,
				assumedAssertionReturnTypes)) {
			return false;
		}
		return true;

	}

	/**
	 * @return true if the names of all assertions following the
	 *         {@code assertThat} - invocation are supported, otherwise false.
	 */
	private static boolean hasSupportedAssertionMethodNames(InvocationChainData invocationChainData) {
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

	private static boolean analyzeFirstAssertionReturnType(ITypeBinding assertThatReturnType,
			ITypeBinding assumedFirstAssertionReturnType) {

		ITypeBinding lhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(assertThatReturnType);
		ITypeBinding rhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(assumedFirstAssertionReturnType);
		return ClassRelationUtil.compareITypeBinding(lhsNonParameterizedTypeErasure, rhsNonParameterizedTypeErasure);

	}

	private static boolean analyzeAssertionMethodReturnTypes(ITypeBinding expectedAssertionReturnType,
			List<ITypeBinding> assertionReturnTypes) {

		return assertionReturnTypes.stream()
			.allMatch(returnType -> ClassRelationUtil.compareITypeBinding(returnType, expectedAssertionReturnType));
	}

	private static ITypeBinding getNonParameterizedTypeErasure(ITypeBinding typeBinding) {
		ITypeBinding erasure = typeBinding.getErasure();
		while (erasure.isParameterizedType()) {
			erasure = erasure.getErasure();
		}
		return erasure;
	}

	private AssertThatInvocationChainAnalyzer() {
		// private constructor to hide the implicit public one.
	}
}
