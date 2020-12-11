package eu.jsparrow.core.visitor.impl.trycatch;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * A visitor for deleting the nodes that are matching with any member of lists
 * provided in the constructor. Intended to be used when a new
 * {@link TryStatement} is created as a copy of the old one, and therefore the
 * close statements and the resource declaration are to be removed.
 * 
 * @author Ardit Ymeri
 * @since 2.4.0
 *
 */
class TwrRemoveNodesVisitor extends ASTVisitor {
	private List<VariableDeclarationFragment> toBeRemoved;
	private List<MethodInvocation> closeStatements;
	private ASTMatcher matcher;

	public TwrRemoveNodesVisitor(List<VariableDeclarationFragment> toBeRemoved,
			List<MethodInvocation> closeStatements) {
		this.toBeRemoved = toBeRemoved;
		this.closeStatements = closeStatements;
		this.matcher = new ASTMatcher();
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		if (!matchesRemoveFragments(fragment)) {
			return false;
		}

		ASTNode parent = fragment.getParent();
		if (ASTNode.VARIABLE_DECLARATION_STATEMENT == parent.getNodeType()
				&& ((VariableDeclarationStatement) parent).fragments()
					.size() == 1) {
			parent.delete();
		} else {
			fragment.delete();
		}

		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		ASTNode parent = node.getParent();
		if (matchesRemoveCloseStatements(node) && parent instanceof Statement) {
			parent.delete();
		}
		return true;
	}

	private boolean matchesRemoveCloseStatements(MethodInvocation node) {
		return closeStatements.stream()
			.anyMatch(methodInvocation -> matcher.match(node, methodInvocation));
	}

	private boolean matchesRemoveFragments(VariableDeclarationFragment fragment) {
		return toBeRemoved.stream()
			.anyMatch(markedForRemoval -> matcher.match(markedForRemoval, fragment));
	}
}
