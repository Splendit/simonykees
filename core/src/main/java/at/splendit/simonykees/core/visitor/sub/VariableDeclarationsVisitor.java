package at.splendit.simonykees.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Gathers the names of the declared variables.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class VariableDeclarationsVisitor extends ASTVisitor {
	private List<SimpleName> variableDelcarations;

	public VariableDeclarationsVisitor() {
		variableDelcarations = new ArrayList<>();
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && resolvedBinding.getKind() == IBinding.VARIABLE && simpleName.isDeclaration()) {
			variableDelcarations.add(simpleName);
		}
		return true;
	}

	public List<SimpleName> getVariableDeclarationNames() {
		return variableDelcarations;
	}
}