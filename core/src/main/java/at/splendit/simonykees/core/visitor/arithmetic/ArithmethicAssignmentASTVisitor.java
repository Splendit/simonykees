package at.splendit.simonykees.core.visitor.arithmetic;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;

import at.splendit.simonykees.core.helper.ArithmeticHelper;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor is build for assignments of arithmetic expressions for base
 * numeric types
 * 
 * 
 * Examples:
 * 
 * a = a + 3; => a += 3;
 * 
 * @author mgh
 *
 */
public class ArithmethicAssignmentASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(Assignment node) {
		if (node.getOperator() != null && node.getOperator().equals(Operator.ASSIGN)) {
			if (node.getLeftHandSide() instanceof SimpleName && node.getRightHandSide() instanceof InfixExpression) {
				SimpleName leftHandSide = (SimpleName) node.getLeftHandSide();

				if (!node.getRightHandSide().resolveTypeBinding().isPrimitive()) {
					return true;
				}

				ArithmeticExpressionASTVisitor arithExpASTVisitor = new ArithmeticExpressionASTVisitor(astRewrite,
						leftHandSide);

				node.getRightHandSide().accept(arithExpASTVisitor);

				if (arithExpASTVisitor.getNewOperator() != null) {
					astRewrite.set(node, Assignment.OPERATOR_PROPERTY,
							ArithmeticHelper.generateOperator(arithExpASTVisitor.getNewOperator()), null);
				}
			}
		}
		return true;
	}
}
