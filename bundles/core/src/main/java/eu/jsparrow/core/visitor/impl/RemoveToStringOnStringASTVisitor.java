package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.constants.ReservedNames;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

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

		Expression variableExpression = node.getExpression();
		List<String> stringFullyQualifiedNameList = generateFullyQualifiedNameList(stringFullyQualifiedName);

		if (!checkSemanticPrecondition(node, variableExpression, stringFullyQualifiedNameList)) {
			return true;
		}

		if (ASTNodeUtil.isFollowedByLineComment(variableExpression, getCommentRewriter())) {
			/*
			 * If the last trailing comment of the expression is a line comment,
			 * then the transformation is avoided as eclipse is placing the rest
			 * of the method invocation in the in the line which is already
			 * commented out.
			 */
			return true;
		}

		boolean unwrapped;
		do {
			unwrapped = false;

			if (variableExpression.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION
					&& node.getLocationInParent() != MethodInvocation.EXPRESSION_PROPERTY) {
				variableExpression = ASTNodeUtil.unwrapParenthesizedExpression(variableExpression);
				unwrapped = true;
			}

			if (variableExpression.getNodeType() == ASTNode.METHOD_INVOCATION) {
				MethodInvocation mI = (MethodInvocation) variableExpression;
				if (StringUtils.equals(ReservedNames.MI_TO_STRING, mI.getName()
					.getFullyQualifiedName()) && mI.typeArguments()
						.isEmpty()
						&& (mI.getExpression() != null && ClassRelationUtil.isContentOfTypes(mI.getExpression()
							.resolveTypeBinding(), stringFullyQualifiedNameList))) {
					variableExpression = mI.getExpression();
					methodInvocationSkipList.add(mI);
					unwrapped = true;
				}
			}
		} while (unwrapped);

		if (isDirectConsumerBody(node, variableExpression)) {
			return true;
		}

		astRewrite.replace(node, astRewrite.createMoveTarget(variableExpression), null);
		saveComments(node, variableExpression);
		onRewrite();

		return true;
	}

	private boolean isDirectConsumerBody(MethodInvocation node, Expression unwrappedExpression) {
		if (node.getLocationInParent() != LambdaExpression.BODY_PROPERTY) {
			return false;
		}

		LambdaExpression lambda = (LambdaExpression) node.getParent();
		ITypeBinding binding = lambda.resolveTypeBinding();
		return ClassRelationUtil.isContentOfType(binding, java.util.function.Consumer.class.getName())
				&& unwrappedExpression.getNodeType() != ASTNode.METHOD_INVOCATION
				&& unwrappedExpression.getNodeType() != ASTNode.CLASS_INSTANCE_CREATION;

	}

	/**
	 * Checks if method invocation is toString. The invocation needs to have
	 * zero arguments. The expressions type where the toString is used on needs
	 * to be a String or a StringLiteral
	 */
	protected boolean checkSemanticPrecondition(MethodInvocation node, Expression methodInvocationexpression,
			List<String> stringFullyQualifiedNameList) {
		ASTNode parent = node.getParent();
		SimpleName name = node.getName();

		List<Type> types = ASTNodeUtil.convertToTypedList(node.typeArguments(), Type.class);

		return StringUtils.equals(ReservedNames.MI_TO_STRING, name.getFullyQualifiedName())
				&& ASTNode.EXPRESSION_STATEMENT != parent.getNodeType() && types.isEmpty()
				&& (methodInvocationexpression != null && ClassRelationUtil
					.isContentOfTypes(methodInvocationexpression.resolveTypeBinding(), stringFullyQualifiedNameList));
	}

	private void saveComments(MethodInvocation node, Expression variableExpression) {
		CommentRewriter cr = getCommentRewriter();
		List<Comment> relatedComments = cr.findRelatedComments(node);
		relatedComments.removeAll(cr.findRelatedComments(variableExpression));
		Statement parentStm = ASTNodeUtil.getSpecificAncestor(node, Statement.class);
		cr.saveBeforeStatement(parentStm, relatedComments);
	}

	@Override
	public void endVisit(MethodInvocation node) {
		methodInvocationSkipList.remove(node);
	}

}
