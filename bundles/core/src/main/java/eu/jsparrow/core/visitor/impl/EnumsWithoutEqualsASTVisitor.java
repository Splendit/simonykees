package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * Looks for occurrences of equals(..) that refer to an Enumeration.
 * <p>
 * Those occurrences are then replaced with ==
 * <ul>
 * <li>Enum: since 1.5, ex.: myEnumInstance.equals(MyEnum.ITEM) ->
 * myEnumInstance == MyEnum.ITEM</li>
 * </ul>
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class EnumsWithoutEqualsASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String EQUALS = "equals"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (methodInvocation.arguments()
			.size() != 1 || methodInvocation.getExpression() == null) {
			return false;
		}
		boolean isEquals = StringUtils.equals(EQUALS, methodInvocation.getName()
			.getFullyQualifiedName());
		if (!isEquals) {
			return false;
		}

		Expression expression = methodInvocation.getExpression();
		ITypeBinding expressionBinding = expression.resolveTypeBinding();
		if (expressionBinding != null && !expressionBinding.isEnum()) {
			return false;
		}
		Expression argument = (Expression) methodInvocation.arguments()
			.get(0);
		ITypeBinding argumentBinding = argument.resolveTypeBinding();
		if (argumentBinding != null && !argumentBinding.isEnum()) {
			return false;
		}

		InfixExpression.Operator newOperator = InfixExpression.Operator.EQUALS;

		if (methodInvocation.getParent()
			.getNodeType() == ASTNode.PREFIX_EXPRESSION
				&& ((PrefixExpression) methodInvocation.getParent()).getOperator() == PrefixExpression.Operator.NOT) {
			newOperator = InfixExpression.Operator.NOT_EQUALS;
		}

		Expression left = (Expression) astRewrite.createMoveTarget(expression);
		Expression right = (Expression) astRewrite.createMoveTarget(argument);
		Expression replacementNode = NodeBuilder.newInfixExpression(methodInvocation.getAST(), newOperator, left,
				right);
		Expression replacedNode = methodInvocation;
		if (methodInvocation.getParent()
			.getNodeType() == ASTNode.PREFIX_EXPRESSION) {
			if (((PrefixExpression) methodInvocation.getParent()).getOperator() == PrefixExpression.Operator.NOT) {
				((InfixExpression) replacementNode).setOperator(InfixExpression.Operator.NOT_EQUALS);
				replacedNode = (Expression) methodInvocation.getParent();
			} else {
				replacementNode = NodeBuilder.newParenthesizedExpression(methodInvocation.getAST(), replacementNode);
			}
		}

		astRewrite.replace(replacedNode, replacementNode, null);
		saveComments(methodInvocation);
		onRewrite();
		addMarkerEvent(replacedNode, replacementNode);
		return false;
	}

	protected void saveComments(MethodInvocation methodInvocation) {
		CommentRewriter commentRewriter = getCommentRewriter();
		commentRewriter.saveCommentsInParentStatement(methodInvocation.getName());
		List<Comment> leadingComments = commentRewriter.findSurroundingComments(methodInvocation);
		Statement parentStm = ASTNodeUtil.getSpecificAncestor(methodInvocation, Statement.class);
		commentRewriter.saveBeforeStatement(parentStm, leadingComments);
	}
}
