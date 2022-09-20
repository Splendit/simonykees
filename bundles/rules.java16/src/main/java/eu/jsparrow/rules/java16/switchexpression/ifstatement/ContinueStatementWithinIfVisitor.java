package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * A visitor to find out whether there is a {@link ContinueStatement} within an
 * {@link ASTNode} which belongs to a loop construct that encloses the visited
 * node. {@link ContinueStatement}s belonging to embedded loops are not counted.
 * 
 * @since 4.3.0
 *
 */
public class ContinueStatementWithinIfVisitor extends ASTVisitor {

	private boolean containingContinueStatement;

	@Override
	public boolean preVisit2(ASTNode node) {
		return !containingContinueStatement;
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
	public boolean visit(TypeDeclarationStatement typeDeclarationStatement) {
		return false;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		containingContinueStatement = true;
		return false;
	}

	public boolean isContainingUnsupportedContinueStatement() {
		return containingContinueStatement;
	}
}
