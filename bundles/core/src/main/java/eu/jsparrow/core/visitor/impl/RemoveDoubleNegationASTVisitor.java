package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.core.markers.common.RemoveDoubleNegationEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * Removes pairs of negations from boolean expressions until only zero or one
 * negation is left.
 * 
 * @since 2.7
 *
 */
public class RemoveDoubleNegationASTVisitor extends AbstractASTRewriteASTVisitor implements RemoveDoubleNegationEvent {

	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		ASTNode replaceNode = unwrapNegations(prefixExpression, true);

		if (prefixExpression != replaceNode) {
			astRewrite.replace(prefixExpression, astRewrite.createCopyTarget(replaceNode), null);
			saveComments(prefixExpression, replaceNode);
			onRewrite();
			addMarkerEvent(prefixExpression);
		}

		return true;
	}

	private void saveComments(PrefixExpression prefixExpression, ASTNode replaceNode) {
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> comments = commentRewriter.findRelatedComments(prefixExpression);
		List<Comment> remainingComents = commentRewriter.findRelatedComments(replaceNode);
		comments.removeAll(remainingComents);
		Statement parentStatement = ASTNodeUtil.getSpecificAncestor(prefixExpression, Statement.class);
		commentRewriter.saveBeforeStatement(parentStatement, comments);
	}

	/**
	 * Recursive search for possible replacement node
	 * 
	 * @param node
	 *            input node
	 * @param selector
	 *            alternating selector for node
	 * @return node to replace the expression with
	 */
	private ASTNode unwrapNegations(ASTNode node, boolean selector) {
		PrefixExpression prefixExpression = null;
		if (ASTNode.PREFIX_EXPRESSION == node.getNodeType()) {
			prefixExpression = (PrefixExpression) node;
		}

		boolean isNotOperator = prefixExpression != null
				&& PrefixExpression.Operator.NOT == prefixExpression.getOperator();

		if (isNotOperator) {
			ASTNode returnValue = unwrapNegations(prefixExpression.getOperand(), !selector);
			if (null == returnValue) {
				return node;
			} else {
				return returnValue;
			}
		} else {
			if (selector) {
				return node;
			}
			return null;
		}
	}
}
