package eu.jsparrow.core.visitor.unused.type;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;

/**
 * Verifies whether a given {@link ASTNode} contains an unexpected
 * {@link AnonymousClassDeclaration} or {@link TypeDeclarationStatement}
 *
 */
public class UnexpectedLocalDeclarationVisitor extends ASTVisitor {
	private boolean unexpectedLocalDeclarationFound;

	@Override
	public boolean preVisit2(ASTNode node) {
		return !unexpectedLocalDeclarationFound;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		unexpectedLocalDeclarationFound = true;
		return false;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		unexpectedLocalDeclarationFound = true;
		return false;
	}

	public boolean isUnexpectedLocalDeclarationFound() {
		return unexpectedLocalDeclarationFound;
	}
}
