package eu.jsparrow.core.visitor.unused.type;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Holds the relevant informations about a compilation unit which is used for
 * JUnit-tests and references a Type declaration.
 * 
 * @since 4.10.0
 */
public class TestReferenceOnType {

	private CompilationUnit compilationUnit;
	private ICompilationUnit iCompilationUnit;

	public TestReferenceOnType(CompilationUnit compilationUnit, ICompilationUnit iCompilationUnit) {
		this.compilationUnit = compilationUnit;
		this.iCompilationUnit = iCompilationUnit;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public ICompilationUnit getICompilationUnit() {
		return iCompilationUnit;
	}
}
