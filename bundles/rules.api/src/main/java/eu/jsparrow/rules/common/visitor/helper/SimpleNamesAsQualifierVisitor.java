package eu.jsparrow.rules.common.visitor.helper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Gathers the names of the declared variables and simple names used as
 * Qualifiers
 * 
 * 
 * @since 3.17.0
 */
public class SimpleNamesAsQualifierVisitor extends ASTVisitor {
	private List<SimpleName> variableDeclarations;

	public SimpleNamesAsQualifierVisitor() {
		variableDeclarations = new ArrayList<>();
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		if (simpleName.getLocationInParent() == QualifiedName.QUALIFIER_PROPERTY) {
			variableDeclarations.add(simpleName);
		} 
		return true;
	}

	public List<SimpleName> getVariableDeclarationNames() {
		return variableDeclarations;
	}
}