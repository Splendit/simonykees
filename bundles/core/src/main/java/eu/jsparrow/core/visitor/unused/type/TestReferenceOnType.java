package eu.jsparrow.core.visitor.unused.type;

import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Holds the relevant informations about a compilation unit which is used for
 * JUnit-tests and references a Type declaration.
 * 
 * @since 4.10.0
 */
public class TestReferenceOnType {

	private boolean removeEntireTest = true;
	private Set<MethodDeclaration> testMethodsReferencingType;
	private Set<AbstractTypeDeclaration> testTypesReferencingType;
	private Set<ImportDeclaration> unusedTypeImportDeclarations;
	private final CompilationUnit compilationUnit;
	private final ICompilationUnit iCompilationUnit;

	public TestReferenceOnType(CompilationUnit compilationUnit,
			ICompilationUnit iCompilationUnit,
			boolean removeEntireTest,
			Set<AbstractTypeDeclaration> typeDeclarations,
			Set<MethodDeclaration> methodDeclarations,
			Set<ImportDeclaration> unusedTypeImportDeclarations) {
		this.compilationUnit = compilationUnit;
		this.iCompilationUnit = iCompilationUnit;
		this.removeEntireTest = removeEntireTest;
		this.testTypesReferencingType = typeDeclarations;
		this.testMethodsReferencingType = methodDeclarations;
		this.unusedTypeImportDeclarations = unusedTypeImportDeclarations;
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

	public Set<MethodDeclaration> getTestMethodsReferencingType() {
		return testMethodsReferencingType;
	}

	public Set<AbstractTypeDeclaration> getTestTypesReferencingType() {
		return testTypesReferencingType;
	}

	public Set<ImportDeclaration> getUnusedTypeImportDeclarations() {
		return unusedTypeImportDeclarations;
	}

}
