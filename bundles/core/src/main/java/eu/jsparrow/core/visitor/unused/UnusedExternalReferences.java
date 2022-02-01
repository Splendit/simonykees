package eu.jsparrow.core.visitor.unused;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;

public class UnusedExternalReferences {

	private CompilationUnit compilationUnit;
	private ICompilationUnit iCompilationUnit;
	private List<SimpleName> unusedReassignments;

	public UnusedExternalReferences(CompilationUnit compilationUnit, ICompilationUnit iCompilationUnit, List<SimpleName> unusedReassignments) {
		this.compilationUnit = compilationUnit;
		this.iCompilationUnit = iCompilationUnit;
		this.unusedReassignments = unusedReassignments;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public List<SimpleName> getUnusedReassignments() {
		return unusedReassignments;
	}
	
	public ICompilationUnit getICompilationUnit() {
		return this.iCompilationUnit;
	}

}
