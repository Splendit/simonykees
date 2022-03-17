package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EmptyStatement;

import eu.jsparrow.core.markers.common.RemoveEmptyStatementEvent;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Finds and removes unnecessary semicolons in code blocks.
 * 
 * @since 2.7.0
 *
 */
public class RemoveEmptyStatementASTVisitor extends AbstractASTRewriteASTVisitor implements RemoveEmptyStatementEvent {

	@Override
	public boolean visit(EmptyStatement emptyStatement) {

		ASTNode parent = emptyStatement.getParent();
		if (ASTNode.BLOCK == parent.getNodeType() || ASTNode.SWITCH_STATEMENT == parent.getNodeType()) {
			astRewrite.remove(emptyStatement, null);
			getCommentRewriter().saveRelatedComments(emptyStatement);
			onRewrite();
			addMarkerEvent(emptyStatement);
		}

		return false;
	}
}
