package eu.jsparrow.core.visitor.unused.method;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class TestSourceReference {

	public TestSourceReference(CompilationUnit compilationUnit, ICompilationUnit iCompilationUnit,
			List<MethodDeclaration> testMethodDeclarations) {
		
	}

	public CompilationUnit getCompilationUnit() {
		return null;
	}

	public ICompilationUnit getICompilationUnit() {
		return null;
	}
	
	public List<MethodDeclaration> getTestDeclarations() {
		return Collections.emptyList();
	}

}
