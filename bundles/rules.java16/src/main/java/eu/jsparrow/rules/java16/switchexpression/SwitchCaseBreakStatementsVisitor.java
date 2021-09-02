package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * A visitor to collect {@link BreakStatement}s of a {@link SwitchStatement}.
 * The {@link BreakStatement}s belonging to embedded loops or other switch
 * statements are not collected.
 * 
 * @since 4.3.0
 *
 */
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
	public boolean visit(AnonymousClassDeclaration acd) {
		return false;
	}

	@Override
	public boolean visit(LambdaExpression lambda) {
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
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

	public List<BreakStatement> getBreakStatements() {
		return this.breakStatements;
	}
}
