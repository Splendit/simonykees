package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * Removes the explicit call to the default constructor of the parent from a
 * child class's constructor.
 * 
 * @since 2.7.0
 *
 */
public class RemoveExplicitCallToSuperASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(SuperConstructorInvocation node) {

		MethodDeclaration parent = ASTNodeUtil.getSpecificAncestor(node, MethodDeclaration.class);
		// if parent is not method declaration or the parent node is not a
		// constructor, return
		if (null == parent || !parent.isConstructor()) {
			return false;
		}

		// if it is not a default constructor invocation, return
		if (null != node.getExpression() || !node.arguments()
			.isEmpty() || !node.typeArguments()
				.isEmpty()) {
			return false;
		}
		CommentRewriter comRewrite = getCommentRewriter();
		comRewrite.saveRelatedComments(node);

		astRewrite.remove(node, null);
		onRewrite();
		return false;
	}
}
