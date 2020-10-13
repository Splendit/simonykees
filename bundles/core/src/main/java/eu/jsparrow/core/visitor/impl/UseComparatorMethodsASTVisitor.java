package eu.jsparrow.core.visitor.impl;

import static java.util.Collections.unmodifiableList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * 
 * @since 3.22.0
 *
 */
public class UseComparatorMethodsASTVisitor extends AbstractAddImportASTVisitor {

	private static final String JAVA_LANG_COMPARABLE = java.lang.Comparable.class.getName();

	@Override
	public boolean visit(LambdaExpression lambda) {

		StructuralPropertyDescriptor locationInParent = lambda.getLocationInParent();
		if (locationInParent != Assignment.RIGHT_HAND_SIDE_PROPERTY
				&& locationInParent != VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			return true;
		}
		MethodInvocation compareToInvocation = extractCompareToInvocation(lambda);
		if (compareToInvocation == null) {
			return true;
		}
		Expression compareToArgument = ASTNodeUtil.convertToTypedList(compareToInvocation.arguments(), Expression.class)
			.get(0);
		Expression compareToExpression = compareToInvocation.getExpression();

		return true;
	}

	private MethodInvocation extractCompareToInvocation(LambdaExpression lambda) {
		ASTNode lambdaBody = lambda.getBody();
		MethodInvocation methodInvocation = null;
		if (lambdaBody.getNodeType() == ASTNode.METHOD_INVOCATION) {
			methodInvocation = (MethodInvocation) lambdaBody;
		}
		if (methodInvocation == null) {
			return null;
		}

		if (!methodInvocation.getName()
			.getIdentifier()
			.equals("compareTo")) {
			return null;
		}

		Expression invocationExpression = methodInvocation.getExpression();
		if (invocationExpression == null) {
			return null;
		}

		ITypeBinding methodExpressionType = invocationExpression.resolveTypeBinding();
		boolean isComparable = ClassRelationUtil.isContentOfType(methodExpressionType, JAVA_LANG_COMPARABLE);
		boolean isInheritingComparable = ClassRelationUtil.isInheritingContentOfTypes(methodExpressionType,
				Collections.singletonList(JAVA_LANG_COMPARABLE));
		if (!isComparable && !isInheritingComparable) {
			return null;
		}

		if (methodInvocation.arguments()
			.size() != 1) {
			return null;
		}

		Expression methodArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		ITypeBinding methodArgumentType = methodArgument.resolveTypeBinding();

		String expressionTypeQualifiedName = methodExpressionType.getQualifiedName();
		String argumentTypeQualifiedName = methodArgumentType.getQualifiedName();
		if (!expressionTypeQualifiedName.equals(argumentTypeQualifiedName)) {
			return null;
		}

		return methodInvocation;
	}

}
