package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class ReferencesVisitor extends ASTVisitor {
	
	private VariableDeclarationFragment  originalFragment;
	private TypeDeclaration originalTypeDeclaration;
	
	private boolean activeReferenceFound = false;
	private List<SimpleName> reassignments = new ArrayList<>();

	public ReferencesVisitor(VariableDeclarationFragment  originalFragment, TypeDeclaration originalTypeDeclaration) {
		this.originalFragment = originalFragment;
		this.originalTypeDeclaration = originalTypeDeclaration;
	}
	
	@Override
	public boolean visit(SimpleName simpleName) {
		/*
		 * TODO: check if the simpleName is a field reference. 
		 */
		return true;
	}
	
	public boolean hasActiveReference() {
		return activeReferenceFound;
	}
	
	public List<SimpleName> getReassignments() {
		return this.reassignments;
	}
}
