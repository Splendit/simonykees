package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Gathers the names of the declared variables and simple names used as
 * Qualifiers
 * 
 * 
 * @since 3.17.0
 */
public class SimpleNamesAsVariableOrQualifierVisitor extends ASTVisitor {
	private List<SimpleName> variableDeclarations;

	public SimpleNamesAsVariableOrQualifierVisitor() {
		variableDeclarations = new ArrayList<>();
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (simpleName.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY) {
			variableDeclarations.add(simpleName);
		} else {
			IBinding resolvedBinding = simpleName.resolveBinding();
			if (resolvedBinding != null && resolvedBinding.getKind() == IBinding.VARIABLE
					&& simpleName.isDeclaration()) {
				variableDeclarations.add(simpleName);
			}
		}
		return true;
	}

	public List<SimpleName> getVariableDeclarationNames() {
		return variableDeclarations;
	}
}