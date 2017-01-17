package at.splendit.simonykees.core.visitor;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Removes all occurrences of StringVariable.concat(Parameter) and transforms
 * them into Infix operation StringVariable + Parameter.
 * 
 * ex.: a.concat(b) -> a + b a.concat(b.concat(c) -> a + b + c
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class StringConcatToPlusASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer STRING_KEY = 1;
	private static final String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	private Set<MethodInvocation> modifyMethodInvocation = new HashSet<>();
	private Expression recursionRightExpression = null;

	public StringConcatToPlusASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (StringUtils.equals("concat", node.getName().getFullyQualifiedName()) //$NON-NLS-1$
				&& (node.getExpression() instanceof SimpleName && ClassRelationUtil
						.isContentOfRegistertITypes(node.getExpression().resolveTypeBinding(), iTypeMap.get(STRING_KEY))
						|| node.getExpression() instanceof StringLiteral)) {
			modifyMethodInvocation.add(node);
		}
		return true;
	}

	@Override
	public void endVisit(MethodInvocation node) {
		if (modifyMethodInvocation.contains(node)) {
			Expression left = (Expression) astRewrite.createMoveTarget(node.getExpression());
			Expression right = (null != recursionRightExpression) ? recursionRightExpression
					: (Expression) astRewrite.createMoveTarget((Expression) node.arguments().get(0));

			Expression replacementNode = NodeBuilder.newInfixExpression(node.getAST(), InfixExpression.Operator.PLUS,
					left, right);

			if (modifyMethodInvocation.contains(node.getParent())) {
				recursionRightExpression = replacementNode;
			} else {
				if (node.getParent() instanceof MethodInvocation) {
					replacementNode = NodeBuilder.newParenthesizedExpression(node.getAST(), replacementNode);
				}
				astRewrite.replace(node, replacementNode, null);
				recursionRightExpression = null;
			}
		}
	}
}
