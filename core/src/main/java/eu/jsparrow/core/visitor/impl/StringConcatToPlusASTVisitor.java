package eu.jsparrow.core.visitor.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.core.util.ClassRelationUtil;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * Removes all occurrences of StringVariable.concat(Parameter) and transforms
 * them into Infix operation StringVariable + Parameter.
 * 
 * ex.: a.concat(b) -> a + b a.concat(b.concat(c) -> a + b + c
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringConcatToPlusASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String STRING_FULLY_QUALLIFIED_NAME = java.lang.String.class.getName();

	private Set<MethodInvocation> modifyMethodInvocation = new HashSet<>();
	private Map<MethodInvocation, Expression> alreadyReplacedExpression = new HashMap<>();

	@Override
	public boolean visit(MethodInvocation node) {
		List<String> fullyQualifiedStringName = generateFullyQualifiedNameList(STRING_FULLY_QUALLIFIED_NAME);
		if (StringUtils.equals("concat", node.getName() //$NON-NLS-1$
			.getFullyQualifiedName()) && ClassRelationUtil.isContentOfTypes(
					node.getExpression()
						.resolveTypeBinding(),
					fullyQualifiedStringName)
				&& ASTNode.EXPRESSION_STATEMENT != node.getParent()
					.getNodeType()
				&& node.arguments()
					.size() == 1
				&& ClassRelationUtil.isContentOfTypes(((Expression) node.arguments()
					.get(0)).resolveTypeBinding(), fullyQualifiedStringName)) {
			modifyMethodInvocation.add(node);
		}
		return true;
	}

	@Override
	public void endVisit(MethodInvocation node) {
		if (modifyMethodInvocation.contains(node)) {
			Expression optionalExpression = node.getExpression();
			Expression argument = (Expression) node.arguments()
				.get(0);

			Expression left = alreadyReplacedExpression.remove(optionalExpression);
			if (null == left) {
				left = (Expression) astRewrite.createMoveTarget(optionalExpression);
			}

			Expression right = alreadyReplacedExpression.remove(argument);
			if (null == right) {
				right = (Expression) astRewrite.createMoveTarget(argument);
			}

			Expression replacementNode = NodeBuilder.newInfixExpression(node.getAST(), InfixExpression.Operator.PLUS,
					left, right);

			if (modifyMethodInvocation.contains(node.getParent())) {
				alreadyReplacedExpression.put(node, replacementNode);
			} else {
				if (node.getParent() instanceof MethodInvocation) {
					replacementNode = NodeBuilder.newParenthesizedExpression(node.getAST(), replacementNode);
				}
				astRewrite.replace(node, replacementNode, null);
				getCommentRewriter().saveCommentsInParentStatement(node);
				onRewrite();
			}
			modifyMethodInvocation.remove(node);

			if (modifyMethodInvocation.isEmpty()) {
				alreadyReplacedExpression.keySet()
					.forEach(key -> {
						astRewrite.replace(key, alreadyReplacedExpression.remove(key), null);
						getCommentRewriter().saveCommentsInParentStatement(key);
						onRewrite();
					});
			}
		}
	}
}
