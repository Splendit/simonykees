package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.YieldStatement;

/**
 * A visitor to collect {@link BreakStatement}s of a {@link SwitchStatement}.
 * The {@link BreakStatement}s belonging to embedded loops or other switch
 * statements are not collected.
 * 
 * @since 4.3.0
 *
 */
public class YieldStatementWithinIfVisitor extends ASTVisitor {

	private boolean containingYieldStatement;

	@Override
	public boolean preVisit2(ASTNode node) {
		return !containingYieldStatement;
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
	public boolean visit(TypeDeclarationStatement typeDeclarationStatement) {
		return false;
	}

	@Override
	public boolean visit(YieldStatement node) {
		containingYieldStatement = true;
		return false;
	}

	public boolean isContainingYieldStatement() {
		return containingYieldStatement;
	}
}
