package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

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

	private static final List<String> JAVA_UTIL_SET = Collections.singletonList(java.util.Set.class.getName());
	private static final List<String> JAVA_UTIL_LIST = Collections.singletonList(java.util.List.class.getName());

	private static final String REMOVE_ALL = "removeAll"; //$NON-NLS-1$
	private static final String REMOVE = "remove"; //$NON-NLS-1$
	private static final String FOR_EACH = "forEach"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (!methodInvocation.getName()
			.getIdentifier()
			.equals(REMOVE_ALL)) {
			return true;
		}
		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null) {
			return true;
		}

		Expression removeAllArgument = ASTNodeUtil.findSingleInvocationArgument(methodInvocation)
			.orElse(null);
		if (removeAllArgument == null) {
			return true;
		}

		if (isSet(methodInvocationExpression) && isList(removeAllArgument)) {
			transform(methodInvocation, methodInvocationExpression, removeAllArgument);
			return false;
		}
		return true;
	}

	private boolean isSet(Expression expression) {
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		return ClassRelationUtil.isInheritingContentOfTypes(typeBinding, JAVA_UTIL_SET)
				|| ClassRelationUtil.isContentOfTypes(typeBinding, JAVA_UTIL_SET);
	}

	private boolean isList(Expression expression) {
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		return ClassRelationUtil.isInheritingContentOfTypes(typeBinding, JAVA_UTIL_LIST) ||
				ClassRelationUtil.isContentOfTypes(typeBinding, JAVA_UTIL_LIST);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void transform(MethodInvocation methodInvocationToReplace, Expression removeAllInvocationExpression,
			Expression removeAllArgument) {

		AST ast = astRewrite.getAST();

		ExpressionMethodReference newExpressionMethodReference = ast.newExpressionMethodReference();
		Expression newSetExpression = (Expression) astRewrite.createCopyTarget(removeAllInvocationExpression);
		newExpressionMethodReference.setExpression(newSetExpression);
		newExpressionMethodReference.setName(ast.newSimpleName(REMOVE));

		MethodInvocation removeAllInvocationReplacement = ast.newMethodInvocation();
		Expression newIterableExpression = (Expression) astRewrite.createCopyTarget(removeAllArgument);
		removeAllInvocationReplacement.setExpression(newIterableExpression);
		removeAllInvocationReplacement.setName(ast.newSimpleName(FOR_EACH));

		List newArguments = removeAllInvocationReplacement.arguments();
		newArguments.add(newExpressionMethodReference);

		astRewrite.replace(methodInvocationToReplace, removeAllInvocationReplacement, null);
		onRewrite();
		addMarkerEvent(methodInvocationToReplace);
	}
}