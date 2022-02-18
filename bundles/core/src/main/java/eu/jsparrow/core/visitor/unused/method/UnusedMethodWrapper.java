package eu.jsparrow.core.visitor.unused.method;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

public class UnusedMethodWrapper {

	public UnusedMethodWrapper(CompilationUnit compilationUnit, JavaAccessModifier accessModifier,
			MethodDeclaration methodDeclaration, List<TestSourceReference> testReferences) {
	}

	public List<TestSourceReference> getTestReferences() {
		// TODO Auto-generated method stub
		return null;
	}

}
