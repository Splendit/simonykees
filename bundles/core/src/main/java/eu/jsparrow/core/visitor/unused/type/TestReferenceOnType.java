package eu.jsparrow.core.visitor.unused.type;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Holds the relevant informations about a compilation unit which is used for
 * JUnit-tests and references a Type declaration.
 * 
 * @since 4.10.0
 */
public class TestReferenceOnType {

	private final boolean removeEntireTest = true;
	private final List<MethodDeclaration> testMethodsReferencingType = new ArrayList<>();
	private final CompilationUnit compilationUnit;
	private final ICompilationUnit iCompilationUnit;

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

	public boolean isRemoveEntireTest() {
		return removeEntireTest;
	}

	public List<MethodDeclaration> getTestMethodsReferencingType() {
		return testMethodsReferencingType;
	}
}
