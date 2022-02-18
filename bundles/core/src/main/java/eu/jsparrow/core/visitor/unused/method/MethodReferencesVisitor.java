package eu.jsparrow.core.visitor.unused.method;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodReferencesVisitor extends ASTVisitor {

	public MethodReferencesVisitor(MethodDeclaration methodDeclaration, AbstractTypeDeclaration typDeclaration,
			Map<String, Boolean> optionsMap) {
		// TODO Auto-generated constructor stub
	}

	public List<MethodDeclaration> getRelatedTestDeclarations() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasMainSourceReference() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasUnresolvedReference() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * Any reference in the main sources shall be considered as an active reference
	 * 
	 */
}
