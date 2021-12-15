package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.markers.common.RemoveNewStringConstructorEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Finds all instantiations of {@link String} with no input parameter (new
 * String()) and all instantiations of {@link String} with a {@link String}
 * parameter (new String("foo")) and replaces those occurrences empty String
 * ("") or a String literal ("foo") respectively.
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class RemoveNewStringConstructorASTVisitor extends AbstractASTRewriteASTVisitor implements RemoveNewStringConstructorEvent {

	private static final String STRING_FULLY_QUALLIFIED_NAME = java.lang.String.class.getName();

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (ClassRelationUtil.isContentOfTypes(node.getType()
			.resolveBinding(), generateFullyQualifiedNameList(STRING_FULLY_QUALLIFIED_NAME))
				&& ASTNode.EXPRESSION_STATEMENT != node.getParent()
					.getNodeType()) {

			/**
			 * node.arguments() javadoc shows that its elements are at least
			 * Expression
			 */
			List<Expression> arguments = (List<Expression>) node.arguments();
			Expression replacement = null;

			do {

				switch (arguments.size()) {

				case 0:
					/**
					 * new String() resolves to ""
					 */
					replacement = node.getAST()
						.newStringLiteral();
					arguments = null;
					break;

				case 1:
					/**
					 * new String("string" || StringLiteral) resolves to
					 * "string" || StringLiteral
					 */
					Expression argument = arguments.get(0);
					arguments = null;
					if (argument instanceof StringLiteral
							|| ClassRelationUtil.isContentOfTypes(argument.resolveTypeBinding(),
									generateFullyQualifiedNameList(STRING_FULLY_QUALLIFIED_NAME))) {
						if (argument instanceof ParenthesizedExpression) {
							argument = ASTNodeUtil.unwrapParenthesizedExpression(argument);
						}
						if (ASTNode.CLASS_INSTANCE_CREATION == argument.getNodeType()
								&& ClassRelationUtil.isContentOfTypes(((ClassInstanceCreation) argument).getType()
									.resolveBinding(), generateFullyQualifiedNameList(STRING_FULLY_QUALLIFIED_NAME))) {
							arguments = (List<Expression>) ((ClassInstanceCreation) argument).arguments();
						}
						replacement = argument;
					}
					break;

				default:
					arguments = null;
					break;
				}
			} while (arguments != null);
			if (replacement != null) {
				astRewrite.replace(node, replacement, null);
				addMarkerEvent(node, replacement);
				getCommentRewriter().saveCommentsInParentStatement(node);
				onRewrite();
			}
		}
		return true;
	}
}
