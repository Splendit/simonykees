package eu.jsparrow.core.visitor.assertj;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * AssertJ provides methods for setting descriptions or error messages of
 * assertions, e.g.: as, describedAs, withFailMessage, overridingErrorMessage.
 * These methods should always be invoked before the actual assertion they
 * intend to describe, otherwise, they have no effect. This rule, swaps the
 * invocation of the assertion methods with the invocation of the methods
 * setting descriptions or the error messages for the corresponding assertions.
 * 
 * For example, the following code:
 * 
 * <pre>
 * assertThat(user.getName())
 * 	.isEqualTo("John")
 * 	.describedAs("Asserting the correct username");
 * </pre>
 * 
 * will be transformed to:
 * 
 * <pre>
 * assertThat(user.getName())
 * 	.isEqualTo("John")
 * 	.describedAs("Asserting the correct username");
 * </pre>
 * 
 * @since 4.6.0
 *
 */
public class ShiftAssertJDescriptionBeforeAssertionASTVisitor extends AbstractASTRewriteASTVisitor {

	@SuppressWarnings("nls")
	private List<String> descriptionSettingMethods = Arrays.asList("as", "describedAs", "withFailMessage",
			"overridingErrorMessage");

	private static final String ORG_ASSERTJ_CORE_API_DESCRIPTABLE = "org.assertj.core.api.Descriptable"; //$NON-NLS-1$
	private static final String ORG_ASSERTJ_CORE_API_ABSTRACT_ASSERT = "org.assertj.core.api.AbstractAssert"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		String methodIdentifier = methodName.getIdentifier();
		if (!descriptionSettingMethods.contains(methodIdentifier)) {
			return true;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (declaringClass == null) {
			return false;
		}

		boolean isAssertJClass = ClassRelationUtil.isContentOfTypes(declaringClass,
				Arrays.asList(ORG_ASSERTJ_CORE_API_ABSTRACT_ASSERT, ORG_ASSERTJ_CORE_API_DESCRIPTABLE));
		if (!isAssertJClass) {
			return true;
		}
		StructuralPropertyDescriptor locInParent = methodInvocation.getLocationInParent();
		if (locInParent != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}

		Expression expression = methodInvocation.getExpression();
		if (expression == null || !isAssertJAssertionInvocation(expression)) {
			return true;
		}

		MethodInvocation assertJAssertion = (MethodInvocation) expression;
		MethodInvocation swappedMethodInvcation = swap(assertJAssertion, methodInvocation);
		astRewrite.replace(methodInvocation, swappedMethodInvcation, null);
		onRewrite();
		return true;
	}

	private MethodInvocation swap(MethodInvocation assertJAssertion, MethodInvocation methodInvocation) {
		MethodInvocation descSetting = AssertionInvocationsUtil.copyMethodInvocationWithoutExpression(methodInvocation,
				astRewrite);
		descSetting.setExpression((Expression) astRewrite.createCopyTarget(assertJAssertion.getExpression()));

		MethodInvocation swapped = AssertionInvocationsUtil.copyMethodInvocationWithoutExpression(assertJAssertion,
				astRewrite);
		swapped.setExpression(descSetting);
		return swapped;
	}

	private boolean isAssertJAssertionInvocation(Expression expression) {
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		SimpleName methodName = methodInvocation.getName();
		String identifier = methodName.getIdentifier();
		return SupportedAssertJAssertions.isSupportedAssertJAssertionMethodName(identifier);
	}

}
