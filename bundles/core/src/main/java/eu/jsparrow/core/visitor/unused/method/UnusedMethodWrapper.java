package eu.jsparrow.core.visitor.unused.method;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

public class UnusedMethodWrapper {
	
	private CompilationUnit compilationUnit;
	private JavaAccessModifier accessModifier;
	private MethodDeclaration methodDeclaration;
	private List<TestSourceReference> testReferences;

	public UnusedMethodWrapper(CompilationUnit compilationUnit, JavaAccessModifier accessModifier,
			MethodDeclaration methodDeclaration, List<TestSourceReference> testReferences) {
		this.compilationUnit = compilationUnit;
		this.accessModifier = accessModifier;
		this.methodDeclaration = methodDeclaration;
		this.testReferences = testReferences;
	}

	public List<TestSourceReference> getTestReferences() {
		return testReferences;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}

	public JavaAccessModifier getAccessModifier() {
		return accessModifier;
	}

	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	@Override
	public String toString() {
		String compilationUnitName = compilationUnit.getJavaElement().getElementName();
		return String.format(
				"UnusedMethodWrapper [compilationUnit=%s, accessModifier=%s, methodDeclaration=%s, testReferences=%s]", //$NON-NLS-1$
				compilationUnitName, accessModifier, methodDeclaration, testReferences);
	}

		
	
}
