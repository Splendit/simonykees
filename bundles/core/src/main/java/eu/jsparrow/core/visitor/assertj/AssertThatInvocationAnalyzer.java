package eu.jsparrow.core.visitor.assertj;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * 
 * @since 4.6.0
 *
 */
class AssertThatInvocationAnalyzer {

	private static final List<String> SUPPORTED_ASSERT_THAT_METHODS = Collections.unmodifiableList(Arrays.asList(
			"assertThat", //$NON-NLS-1$
			"assertThatCode", //$NON-NLS-1$
			"assertThatThrownBy", //$NON-NLS-1$
			"assertThatObject"//$NON-NLS-1$
	));

	static Optional<MethodInvocation> findSupportedAssertThatInvocation(
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
		if (ClassRelationUtil.isContentOfType(declaringClass,
				"org.assertj.core.api.Assertions") //$NON-NLS-1$
				|| ClassRelationUtil.isContentOfType(declaringClass,
						"org.assertj.core.api.AssertionsForClassTypes") //$NON-NLS-1$
				|| ClassRelationUtil.isContentOfType(declaringClass,
						"org.assertj.core.api.AssertionsForInterfaceTypes")) { //$NON-NLS-1$
			return Optional.of(assumedAssertThatInvocation);
		}
		return Optional.empty();
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

	private AssertThatInvocationAnalyzer() {
		// private constructor to hide the implicit public one.
	}
}
