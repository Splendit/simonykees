package eu.jsparrow.rules.common.visitor.helper;

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
	private List<SimpleName> variableDeclarations;

	public VariableDeclarationsVisitor() {
		variableDeclarations = new ArrayList<>();
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		IBinding resolvedBinding = simpleName.resolveBinding();
		if (resolvedBinding != null && resolvedBinding.getKind() == IBinding.VARIABLE && simpleName.isDeclaration()) {
			variableDeclarations.add(simpleName);
		}
		return true;
	}

	public List<SimpleName> getVariableDeclarationNames() {
		return variableDeclarations;
	}
}