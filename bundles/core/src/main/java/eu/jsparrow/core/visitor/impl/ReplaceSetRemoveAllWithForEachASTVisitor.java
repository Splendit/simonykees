package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.exception.visitor.UnresolvedTypeBindingException;
import eu.jsparrow.core.markers.common.ReplaceSetRemoveAllWithForEachEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 4.13.0
 * 
 */
public class ReplaceSetRemoveAllWithForEachASTVisitor extends AbstractASTRewriteASTVisitor
		implements ReplaceSetRemoveAllWithForEachEvent {

	private static final List<String> LIST_WITH_JAVA_LANG_ITERABLE_CLASS_NAME = Collections
		.singletonList(java.lang.Iterable.class.getName());

	private static final List<String> LIST_WITH_JAVA_UTIL_SET_CLASS_NAME = Collections
		.singletonList(java.util.Set.class.getName());

	private static final String REMOVE_ALL = "removeAll"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null) {
			return true;
		}
		if (!methodInvocation.getName()
			.getIdentifier()
			.equals(REMOVE_ALL)) {
			return true;
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return true;
		}
		Expression removeAllArgument = arguments.get(0);
		try {
			if(methodInvocation.resolveMethodBinding() == null) {
				String message = "Could not resolve type binding of method invocation" //$NON-NLS-1$
						+ methodInvocation.toString();
				throw new UnresolvedTypeBindingException(message);
			}
			if (isRemoveAllInvocationWithSlowPerformance(methodInvocationExpression, removeAllArgument)) {
				transform(methodInvocation, methodInvocationExpression, removeAllArgument);
				return false;
			}
		} catch (Exception exc) {
			return false;
		}
		return true;
	}

	private boolean isRemoveAllInvocationWithSlowPerformance(Expression methodInvocationExpression,
			Expression removeAllArgument) throws UnresolvedTypeBindingException {
		ITypeBinding invocationExpressionType = methodInvocationExpression.resolveTypeBinding();
		if (invocationExpressionType == null) {
			String message = "Could not resolve type binding of method invocation expression: " //$NON-NLS-1$
					+ methodInvocationExpression.toString();
			throw new UnresolvedTypeBindingException(message);
		}
		ITypeBinding invocationArgumentType = removeAllArgument.resolveTypeBinding();
		if (invocationArgumentType == null) {
			String message = "Could not resolve type binding of method invocation argument: " //$NON-NLS-1$
					+ removeAllArgument.toString();
			throw new UnresolvedTypeBindingException(message);
		}
		return isInvocationOnSet(invocationExpressionType) && isIterableArgument(invocationArgumentType);

	}

	private boolean isInvocationOnSet(ITypeBinding invocationExpressionType) {
		return ClassRelationUtil.isInheritingContentOfTypes(invocationExpressionType,
				LIST_WITH_JAVA_UTIL_SET_CLASS_NAME)
				|| ClassRelationUtil.isContentOfTypes(invocationExpressionType, LIST_WITH_JAVA_UTIL_SET_CLASS_NAME);
	}

	private boolean isIterableArgument(ITypeBinding invocationArgumentType) {
		return ClassRelationUtil.isInheritingContentOfTypes(invocationArgumentType,
				LIST_WITH_JAVA_LANG_ITERABLE_CLASS_NAME) ||
				ClassRelationUtil.isContentOfTypes(invocationArgumentType,
						LIST_WITH_JAVA_LANG_ITERABLE_CLASS_NAME);
	}

	private void transform(MethodInvocation methodInvocationToReplace, Expression setExpression,
			Expression removeAllArgument) {
		// This method is not implemented yet !!!
		methodInvocationToReplace.toString();
		setExpression.toString();
		removeAllArgument.toString();
	}
}