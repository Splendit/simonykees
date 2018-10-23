package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * ASTVisitor that searches control statements for non-block bodies and wraps it
 * into a block.
 * 
 * @since 2.7
 *
 */
public class RemoveDoubleNegationASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(PrefixExpression prefixExpression) {
		ASTNode replaceNode = unwrapNegations(prefixExpression, true);

		if (null != replaceNode && prefixExpression != replaceNode) {
			astRewrite.replace(prefixExpression, astRewrite.createCopyTarget(replaceNode), null);
			saveComments(prefixExpression, replaceNode);
			onRewrite();
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
