package at.splendit.simonykees.core;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IfStatement;

public class RefactorASTVisitor extends ASTVisitor {

	@Override
	public boolean visit(IfStatement node) {
		RefactorHandler.log(IStatus.INFO, "visit(IfStatement node) [" + node.toString() + "]", null);
		return true;
	}

	@Override
	public void endVisit(IfStatement node) {
		RefactorHandler.log(IStatus.INFO, "endVisit(IfStatement node) [" + node.toString() + "]", null);
	}
	
	

}
