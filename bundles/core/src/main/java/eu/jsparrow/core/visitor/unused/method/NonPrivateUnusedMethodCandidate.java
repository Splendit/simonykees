package eu.jsparrow.core.visitor.unused.method;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import eu.jsparrow.core.visitor.renaming.JavaAccessModifier;

/**
 * Holds the relevant information for a method declaration that could
 * potentially be not used activelly.
 * 
 * @since 4.9.0
 */
public class NonPrivateUnusedMethodCandidate {

	private MethodDeclaration methodDeclaration;
	private JavaAccessModifier accessModifier;

	public NonPrivateUnusedMethodCandidate(MethodDeclaration methodDeclaration, JavaAccessModifier accessModifier) {
		this.methodDeclaration = methodDeclaration;
		this.accessModifier = accessModifier;
	}

	public MethodDeclaration getDeclaration() {
		return methodDeclaration;
	}

	public JavaAccessModifier getAccessModifier() {
		return accessModifier;
	}

}
