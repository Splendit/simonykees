package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.markers.common.CollectionRemoveAllEvent;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * An Collection that removes it from itself is replaced with clear
 * collectionName.removeAll(collectionName) -> collectionName.clear()
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class CollectionRemoveAllASTVisitor extends AbstractASTRewriteASTVisitor implements CollectionRemoveAllEvent {

	private static final Logger logger = LoggerFactory.getLogger(CollectionRemoveAllASTVisitor.class);

	private static String collectionFullyQualifiedName = java.util.Collection.class.getName();

	private ASTMatcher astMatcher = new ASTMatcher();

	@Override
	public boolean visit(MethodInvocation node) {
		if (StringUtils.equals("removeAll", node.getName() //$NON-NLS-1$
			.getFullyQualifiedName()) && node.getExpression() instanceof SimpleName
				&& ClassRelationUtil.isInheritingContentOfTypes(node.getExpression()
					.resolveTypeBinding(), Collections.singletonList(collectionFullyQualifiedName))) {

			@SuppressWarnings("unchecked")
			List<Expression> arguments = node.arguments();
			if (arguments.size() == 1 && arguments.get(0) instanceof SimpleName
					&& astMatcher.match((SimpleName) arguments.get(0), node.getExpression())) {
				logger.debug("replace statement"); //$NON-NLS-1$

				SimpleName clear = node.getAST()
					.newSimpleName("clear"); //$NON-NLS-1$
				MethodInvocation newMI = NodeBuilder.newMethodInvocation(node.getAST(),
						(Expression) astRewrite.createMoveTarget(node.getExpression()), clear);
				astRewrite.replace(node, newMI, null);
				onRewrite();
				addMarkerEvent(node);
				saveComments(node);
			}
		}
		return true;
	}

	private void saveComments(MethodInvocation node) {
		CommentRewriter commRewriter = getCommentRewriter();
		List<Comment> internalComments = commRewriter.findInternalComments(node);
		internalComments.removeAll(commRewriter.findRelatedComments(node.getExpression()));
		Statement statement = ASTNodeUtil.getSpecificAncestor(node, Statement.class);
		commRewriter.saveBeforeStatement(statement, internalComments);
	}
}
