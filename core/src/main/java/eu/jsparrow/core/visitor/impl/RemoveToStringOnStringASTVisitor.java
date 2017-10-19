package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;

import eu.jsparrow.core.constants.ReservedNames;
import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.util.ClassRelationUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Every usage of the function {@link Object#toString()} on a Java Object is
 * removed, if it is used on an element with the type String
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveToStringOnStringASTVisitor extends AbstractASTRewriteASTVisitor {

	private static String stringFullyQualifiedName = java.lang.String.class.getName();

	private List<MethodInvocation> methodInvocationSkipList;

	public RemoveToStringOnStringASTVisitor() {
		this.methodInvocationSkipList = new ArrayList<>();
	}

	@Override
	public boolean visit(MethodInvocation node) {

		/*
		 * MethodInvocation already handled
		 */
		if (methodInvocationSkipList.contains(node)) {
			return true;
		}
		
		List<String> stringFullyQualifiedNameList = generateFullyQualifiedNameList(stringFullyQualifiedName);

		/*
		 * Checks if method invocation is toString. The invocation needs to have
		 * zero arguments. The expressions type where the toString is used on
		 * needs to be a String or a StringLiteral
		 */
		if (StringUtils.equals(ReservedNames.MI_TO_STRING, node.getName().getFullyQualifiedName())
				&& !(node.getParent() instanceof ExpressionStatement) && node.typeArguments().isEmpty()
				&& (node.getExpression() != null && ClassRelationUtil.isContentOfTypes(
						node.getExpression().resolveTypeBinding(), stringFullyQualifiedNameList))) {
			
			Expression variableExpression = node.getExpression();

			boolean unwrapped = false;
			do {
				unwrapped = false;
				if (variableExpression instanceof ParenthesizedExpression) {
					variableExpression = ASTNodeUtil.unwrapParenthesizedExpression(variableExpression);
					unwrapped = true;
				}

				if (variableExpression instanceof MethodInvocation) {
					MethodInvocation mI = (MethodInvocation) variableExpression;
					if (StringUtils.equals(ReservedNames.MI_TO_STRING, mI.getName().getFullyQualifiedName())
							&& mI.typeArguments().isEmpty()
							&& (mI.getExpression() != null && ClassRelationUtil.isContentOfTypes(
									mI.getExpression().resolveTypeBinding(), stringFullyQualifiedNameList))) {
						variableExpression = mI.getExpression();
						methodInvocationSkipList.add(mI);
						unwrapped = true;
					}
				}
			} while (unwrapped);

			astRewrite.replace(node, (Expression) astRewrite.createMoveTarget(variableExpression), null);

		}
		return true;
	}

	@Override
	public void endVisit(MethodInvocation node) {
		methodInvocationSkipList.remove(node);
	}

}
