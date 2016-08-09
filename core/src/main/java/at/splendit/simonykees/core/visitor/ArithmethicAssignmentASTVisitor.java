package at.splendit.simonykees.core.visitor;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.helper.ArithmeticHelper;

public class ArithmethicAssignmentASTVisitor extends ASTVisitor {

	private ASTRewrite astRewrite;

	public ArithmethicAssignmentASTVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}

	@Override
	public boolean visit(Assignment node) {
		if (node.getOperator() != null && node.getOperator().equals(Operator.ASSIGN)) {
			if (node.getLeftHandSide() instanceof SimpleName && node.getRightHandSide() instanceof InfixExpression) {
				SimpleName leftHandSide = (SimpleName) node.getLeftHandSide();
				InfixExpression rightHandSide = (InfixExpression) node.getRightHandSide();
				Pair<InfixExpression, Expression> nodesToChange = ArithmeticHelper.extractSimpleName(leftHandSide,
						rightHandSide);
				Assignment replacementNode = (Assignment) ASTNode.copySubtree(node.getAST(), node);
				
				replacementNode.setOperator(generateOperator(nodesToChange.getLeft().getOperator()));
				astRewrite.replace(nodesToChange.getLeft(), nodesToChange.getRight(), null);
				astRewrite.replace(node, replacementNode, null);
			}
		}
		return true;
	}

	private Assignment.Operator generateOperator(InfixExpression.Operator oldOperator) {
		if (InfixExpression.Operator.PLUS.equals(oldOperator)) {
			return Assignment.Operator.PLUS_ASSIGN;
		} else if (InfixExpression.Operator.MINUS.equals(oldOperator)) {
			return Assignment.Operator.MINUS_ASSIGN;
		} else if (InfixExpression.Operator.TIMES.equals(oldOperator)) {
			return Assignment.Operator.TIMES_ASSIGN;
		} else if (InfixExpression.Operator.DIVIDE.equals(oldOperator)) {
			return Assignment.Operator.DIVIDE_ASSIGN;
		}
		return null;
	}

}
