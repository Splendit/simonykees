package eu.jsparrow.core.visitor.unused.method;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class TestSourceReference {

	private CompilationUnit compilationUnit;
	private ICompilationUnit iCompilationUnit;
	private Set<MethodDeclaration> testMethodDeclarations;

	public TestSourceReference(CompilationUnit compilationUnit, ICompilationUnit iCompilationUnit,
			Set<MethodDeclaration> testMethodDeclarations) {
		this.compilationUnit = compilationUnit;
		this.iCompilationUnit = iCompilationUnit;
		this.testMethodDeclarations = testMethodDeclarations;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public ICompilationUnit getICompilationUnit() {
		return iCompilationUnit;
	}

	public Set<MethodDeclaration> getTestDeclarations() {
		return testMethodDeclarations;
	}

}
