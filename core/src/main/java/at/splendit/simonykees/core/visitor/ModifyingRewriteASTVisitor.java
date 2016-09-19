package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;

import at.splendit.simonykees.core.Activator;

@Deprecated
public class ModifyingRewriteASTVisitor extends ASTVisitor {
	
	@Deprecated
	public ModifyingRewriteASTVisitor() {	}

	@Override
	public boolean visit(IfStatement node) {
		//RefactorHandler.log(IStatus.INFO, "visit(IfStatement node) [" + node.toString() + "]", null);
		return true;
	}

	@Override
	public void endVisit(IfStatement node) {
		//RefactorHandler.log(IStatus.INFO, "endVisit(IfStatement node) [" + node.toString() + "]", null);
	}
	
	
	@Override
	public boolean visit(Assignment node) {
		if(node.getOperator() != null && node.getOperator().equals(Operator.ASSIGN)) {
			// TODO check if node.getNodeType() is also suitable!
			SimpleName leftHandSide = (SimpleName) node.getLeftHandSide();
			Expression rightHandSide = node.getRightHandSide();
			if (rightHandSide instanceof InfixExpression) {
				Expression left = ((InfixExpression) rightHandSide).getLeftOperand();
				Expression right = ((InfixExpression) rightHandSide).getRightOperand();
				if (left instanceof SimpleName) {
					SimpleName name = (SimpleName) left;
					if (leftHandSide.getIdentifier().equals(name.getIdentifier())) {
						node.setOperator(Operator.PLUS_ASSIGN);
						node.setRightHandSide(node.getRoot().getAST().newNumberLiteral(((NumberLiteral) right).getToken()));
					}
				}
			} else if (rightHandSide instanceof NumberLiteral) {
				((NumberLiteral) rightHandSide).getToken();
			} else {
				Activator.log("implement [" + rightHandSide.getClass().getSimpleName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			}
//			node.setOperator(Operator.PLUS_ASSIGN);
		}
		// TODO true or false?
		return true;
	}
	

}
