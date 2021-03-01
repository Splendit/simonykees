package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Replaces static invocation of
 * {@link Collections#sort(List, java.util.Comparator)} with
 * {@link List#sort(java.util.Comparator)}.
 * 
 * @since 3.6.0
 */
public class UseListSortASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String SORT = "sort"; //$NON-NLS-1$
	private static final String JAVA_UTIL_LIST = java.util.List.class.getName();
	private static final String JAVA_UTIL_COLLECTIONS = java.util.Collections.class.getName();
	private static final String JAVA_UTIL_COMPARATOR = java.util.Comparator.class.getName();

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		if (!SORT.equals(methodName.getIdentifier())) {
			return true;
		}
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 2) {
			return true;
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if(methodBinding == null) {
			return true;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass, JAVA_UTIL_COLLECTIONS)) {
			return true;
		}

		Expression firstArgument = arguments.get(0);
		ITypeBinding firstArgumentType = firstArgument.resolveTypeBinding();
		if (!ClassRelationUtil.isInheritingContentOfTypes(firstArgumentType, Collections.singletonList(JAVA_UTIL_LIST))
				&& !ClassRelationUtil.isContentOfType(firstArgumentType, JAVA_UTIL_LIST)) {
			return true;
		}

		Expression secondArgument = arguments.get(1);
		ITypeBinding secondArgumentType = secondArgument.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(secondArgumentType, JAVA_UTIL_COMPARATOR) 
				&& !ClassRelationUtil.isInheritingContentOfTypes(secondArgumentType, Collections.singletonList(JAVA_UTIL_COMPARATOR))) {
			return true;
		}

		Expression newExpression = (Expression) astRewrite.createMoveTarget(firstArgument);
		Expression expression = methodInvocation.getExpression();
		if (expression == null) {
			/*
			 * The method could be statically imported
			 */
			astRewrite.set(methodInvocation, MethodInvocation.EXPRESSION_PROPERTY, newExpression, null);
		} else {
			astRewrite.replace(expression, newExpression, null);
		}
		onRewrite();

		return false;
	}

}
