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

	static Optional<ITypeBinding> findSupportedAssertionReturnType(InvocationChainData invocationChainData) {
		List<ITypeBinding> assertionTypes = invocationChainData.getSubsequentInvocations()
			.stream()
			.map(MethodInvocation::resolveTypeBinding)
			.collect(Collectors.toList());

		Optional<ITypeBinding> returnValue = assertionTypes.stream()
			.findFirst();

		ITypeBinding firstTypeBinding = returnValue.orElse(null);
		if (firstTypeBinding == null) {
			return Optional.empty();
		}
		if (ClassRelationUtil.isContentOfType(firstTypeBinding, "void")) { //$NON-NLS-1$
			return Optional.empty();
		}
		
		for(int i = 1; i < assertionTypes.size(); i++) {
			if(!ClassRelationUtil.compareITypeBinding(firstTypeBinding, assertionTypes.get(i))) {
				return Optional.empty();
			}			
		}
		return returnValue;
	}

	private AssertThatInvocationChainAnalyzer() {
		// private constructor to hide the implicit public one.
	}
}
