package eu.jsparrow.core.visitor.unused;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;

public class UnusedExternalReferences {

	private CompilationUnit compilationUnit;
	private List<SimpleName> unusedReassignments;

	public UnusedExternalReferences(CompilationUnit compilationUnit, List<SimpleName> unusedReassignments) {
		super();
		this.compilationUnit = compilationUnit;
		this.unusedReassignments = unusedReassignments;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public List<SimpleName> getUnusedReassignments() {
		return unusedReassignments;
	}

}
