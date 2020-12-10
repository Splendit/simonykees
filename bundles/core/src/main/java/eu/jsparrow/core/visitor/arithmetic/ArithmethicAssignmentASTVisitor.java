package eu.jsparrow.core.visitor.arithmetic;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.OperatorUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor is build for assignments of arithmetic expressions for base
 * numeric types. The only transformed expressions are those with only two
 * operands. An parenthesis is counted as one operand. Complex arithmetic
 * expressions are not evaluated, because the risk of an miscalculation is to
 * high.
 * 
 * Examples:
 * 
 * a = a + 3; =&gt; a += 3; a = a + (3 * 3); =&gt; a += (3 * 3);
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class ArithmethicAssignmentASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(Assignment node) {
		if (node.getOperator() != null && node.getOperator()
			.equals(Operator.ASSIGN) && node.getLeftHandSide() instanceof SimpleName
				&& node.getRightHandSide() instanceof InfixExpression) {
			SimpleName leftHandSide = (SimpleName) node.getLeftHandSide();

			// TODO verbessern, dass alle kombinationen von typen abgefangen
			// werden, die
			// nicht weiter untersucht werden sollen.
			if (node.getRightHandSide()
				.resolveTypeBinding() != null
					&& !node.getRightHandSide()
						.resolveTypeBinding()
						.isPrimitive()
					|| node.getLeftHandSide()
						.resolveTypeBinding() != null
							&& !node.getLeftHandSide()
								.resolveTypeBinding()
								.isPrimitive()) {
				return true;
			}

			ArithmeticExpressionVisitor arithExpASTVisitor = new ArithmeticExpressionVisitor(astRewrite,
					leftHandSide);

			node.getRightHandSide()
				.accept(arithExpASTVisitor);

			if (arithExpASTVisitor.getNewOperator() != null) {
				astRewrite.set(node, Assignment.OPERATOR_PROPERTY,
						OperatorUtil.generateOperator(arithExpASTVisitor.getNewOperator()), null);
				getCommentRewriter().saveRelatedComments(node.getRightHandSide(),
						ASTNodeUtil.getSpecificAncestor(node, Statement.class));
				onRewrite();
			}
		}
		return true;
	}
}
