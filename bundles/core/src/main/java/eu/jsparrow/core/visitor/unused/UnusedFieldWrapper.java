package eu.jsparrow.core.visitor.unused;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

public class UnusedFieldWrapper {

	private VariableDeclarationFragment fragment;
	private List<SimpleName> internalReassignments;
	private List<UnusedExternalReferences> unusedExternalReferences = new ArrayList<>();

	public UnusedFieldWrapper(VariableDeclarationFragment fragment, List<SimpleName> internalReassignments,
			List<UnusedExternalReferences> unusedExternalReferences) {
		this.fragment = fragment;
		this.internalReassignments = internalReassignments;
		this.unusedExternalReferences = unusedExternalReferences;
	}

	public VariableDeclarationFragment getFragment() {
		return fragment;
	}

	public List<SimpleName> getUnusedReassignments() {
		return internalReassignments;
	}

	public List<UnusedExternalReferences> getUnusedExternalReferences() {
		return unusedExternalReferences;
	}

	public CompilationUnit getCompilationUnit() {
		return null;
	}

	public JavaAccessModifier getFieldModifier() {
		return null;
	}

	public IPath getDeclarationPath() {
		return null;
	}

	public String getFieldName() {
		return null;
	}

	public String getClassDeclarationName() {
		return null;
	}
}
