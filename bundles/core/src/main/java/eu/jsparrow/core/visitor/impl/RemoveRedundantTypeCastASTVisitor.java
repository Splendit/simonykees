package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * The visitor first searches the next type cast operation. If the expression is
 * casted to a type which already is exactly the type of the expression, then
 * the type casting prefix is removed. Additionally, also parentheses will be
 * removed if they are not necessary any more. <br>
 * This rule regards two types as exactly the same only when both have also
 * exactly the same generic arguments.
 * <p>
 * Example:
 * <p>
 * {@code ((String)"HelloWorld").charAt(0);}<br>
 * is transformed to <br>
 * {@code "HelloWorld".charAt(0);} <br>
 * 
 * @since 3.14.0
 *
 */
public class RemoveRedundantTypeCastASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(CastExpression castExpression) {
		ITypeBinding typeFrom = castExpression.getExpression()
			.resolveTypeBinding();
		ITypeBinding typeTo = castExpression.getType()
			.resolveBinding();

		if (ClassRelationUtil.compareITypeBinding(typeFrom, typeTo)) {
			applyRule(castExpression);
		}
		return true;
	}

	private void applyRule(CastExpression typeCast) {
		ASTNode nodeToBeReplaced = getASTNodeToBeReplaced(typeCast);
		ASTNode replacement = astRewrite.createCopyTarget(getASTNodeReplacement(typeCast));

		astRewrite.replace(nodeToBeReplaced, replacement, null);
		onRewrite();

	}

	private static ASTNode getASTNodeToBeReplaced(CastExpression typeCast) {

		ASTNode nodeToBeReplaced = typeCast;
		while (nodeToBeReplaced.getParent()
			.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			nodeToBeReplaced = nodeToBeReplaced.getParent();
		}
		return nodeToBeReplaced;
	}

	private static ASTNode getASTNodeReplacement(CastExpression typeCast) {
		Expression expressionToBeCasted = typeCast.getExpression();
		int typeCastArgumentNodeType = expressionToBeCasted.getNodeType();
		if (typeCastArgumentNodeType != ASTNode.PARENTHESIZED_EXPRESSION) {
			return expressionToBeCasted;
		}
		ASTNode typeCastParent = typeCast.getParent();
		while (typeCastParent.getNodeType() == ASTNode.PARENTHESIZED_EXPRESSION) {
			typeCastParent = typeCastParent.getParent();
		}
		int TypeCastParentNodeType = typeCastParent
			.getNodeType();
		if (TypeCastParentNodeType != ASTNode.VARIABLE_DECLARATION_FRAGMENT
				&& TypeCastParentNodeType != ASTNode.ASSIGNMENT) {
			return expressionToBeCasted;
		}

		return ASTNodeUtil.unwrapParenthesizedExpression(expressionToBeCasted);
	}

}
