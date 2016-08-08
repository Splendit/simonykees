package at.splendit.simonykees.core.visitor;

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

public class AstRewriteAstVisitor extends ASTVisitor {
	
	private ASTRewrite astRewrite;
	
	public AstRewriteAstVisitor(ASTRewrite astRewrite) {
		 this.astRewrite = astRewrite;
	}

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
//						node.setOperator(Operator.PLUS_ASSIGN);
//						node.setRightHandSide(node.getRoot().getAST().newNumberLiteral(((NumberLiteral) right).getToken()));
						
//						Assignment newNode = (Assignment) astRewrite.createCopyTarget(node);
						Assignment newNode = node.getAST().newAssignment();
//						Assignment newNode = (Assignment) ASTNode.copySubtree(node.getAST(), node);
						newNode.setOperator(Operator.PLUS_ASSIGN);
						newNode.setLeftHandSide(newNode.getRoot().getAST().newSimpleName(leftHandSide.getIdentifier()));
						newNode.setRightHandSide(newNode.getRoot().getAST().newNumberLiteral(((NumberLiteral) right).getToken()));
						
						astRewrite.replace(node, newNode, null);
					}
				}
			} else if (rightHandSide instanceof NumberLiteral) {
				((NumberLiteral) rightHandSide).getToken();
			} else {
				Activator.log("implement [" + rightHandSide.getClass().getSimpleName() + "]");
			}
//			node.setOperator(Operator.PLUS_ASSIGN);
		}
		// TODO true or false?
		return false;
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		
		SimpleName oldName = node.getName();
		
		SimpleName newName = node.getRoot().getAST().newSimpleName("Y");
		astRewrite.replace(oldName, newName, null);
		
		return true;
	}

}
