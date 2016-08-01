package at.splendit.simonykees.core;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.IfStatement;

public class RefactorASTVisitor extends ASTVisitor {

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
		if(node.getOperator() != null && node.getOperator().equals(Operator.ASSIGN)){
			node.setOperator(Operator.PLUS_ASSIGN);
		}
		return true;
	}
	

}
