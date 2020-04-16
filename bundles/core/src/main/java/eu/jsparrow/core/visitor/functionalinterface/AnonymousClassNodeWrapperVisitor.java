package eu.jsparrow.core.visitor.functionalinterface;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;

/**
 * A visitor for checking whether a node is an ancestor of the anonymous class
 * given in the construct.
 * 
 * @author Ardit Ymeri
 * @since 2.0
 *
 */
class AnonymousClassNodeWrapperVisitor extends ASTVisitor {
	private AnonymousClassDeclaration node;
	private boolean isAncestorOfNode = false;

	public AnonymousClassNodeWrapperVisitor(AnonymousClassDeclaration node) {
		this.node = node;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		if (this.node == node) {
			isAncestorOfNode = true;
		}
		return true;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !isAncestorOfNode;
	}

	public boolean isAncestor() {
		return isAncestorOfNode;
	}

}