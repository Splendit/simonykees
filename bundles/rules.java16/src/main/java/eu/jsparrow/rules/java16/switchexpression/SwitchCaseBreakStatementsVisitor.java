package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;

public class SwitchCaseBreakStatementsVisitor extends ASTVisitor {

	private List<BreakStatement> breakStatements = new ArrayList<>();
	
	@Override
	public boolean visit(SwitchStatement switchStatement) {
		return false;
	}
	
	@Override
	public boolean visit(SwitchExpression switchExpression) {
		return false;
	}
	
	@Override
	public boolean visit(WhileStatement whileStatement) {
		return false;
	}
	
	@Override
	public boolean visit(ForStatement forStatement) {
		return false;
	}
	
	@Override
	public boolean visit(EnhancedForStatement forStatement) {
		return false;
	}
	
	@Override
	public boolean visit(DoStatement doStatement) {
		return false;
	}
	
	@Override
	public boolean visit(BreakStatement breakStatement) {
		breakStatements.add(breakStatement);
		return true;
	}
	
	public boolean hasMultipleBreakStatements() {
		return this.breakStatements.size() > 1;
	}
}
