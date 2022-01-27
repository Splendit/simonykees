package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class UnusedFieldWrapper {

	private VariableDeclarationFragment fragment;
	private List<SimpleName> unusedReassignments;
	private List<UnusedExternalReferences> unusedExternalReferences = new ArrayList<>();
	
	public UnusedFieldWrapper(VariableDeclarationFragment fragment, List<SimpleName> unusedReassignments) {
		this.fragment = fragment;
		this.unusedReassignments = unusedReassignments;
	}
	
	public VariableDeclarationFragment getFragment() {
		return fragment;
	}
	
	public List<SimpleName> getUnusedReassignments() {
		return unusedReassignments;
	}
	
	public List<UnusedExternalReferences> getUnusedExternalReferences() {
		return unusedExternalReferences;
	}
	
	public CompilationUnit getCompilationUnit() {
		return null;
	}
		
}
