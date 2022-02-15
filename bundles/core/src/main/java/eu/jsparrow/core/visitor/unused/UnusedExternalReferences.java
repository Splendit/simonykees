package eu.jsparrow.core.visitor.unused;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;

public class UnusedExternalReferences {

	private CompilationUnit compilationUnit;
	private ICompilationUnit iCompilationUnit;
	private List<ExpressionStatement> unusedReassignments;

	public UnusedExternalReferences(CompilationUnit compilationUnit, ICompilationUnit iCompilationUnit,
			List<ExpressionStatement> unusedReassignments) {
		this.compilationUnit = compilationUnit;
		this.iCompilationUnit = iCompilationUnit;
		this.unusedReassignments = unusedReassignments;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public List<ExpressionStatement> getUnusedReassignments() {
		return unusedReassignments;
	}

	public ICompilationUnit getICompilationUnit() {
		return this.iCompilationUnit;
	}

	@Override
	public String toString() {
		return String.format(
				"UnusedExternalReferences [iCompilationUnit=%s, unusedReassignments=%s]", //$NON-NLS-1$
				iCompilationUnit.getElementName(), unusedReassignments);
	}
}
