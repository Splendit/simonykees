package eu.jsparrow.core.visitor.assertj;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Wrapper class storing all informations about an AssertJ-{@link assertThat} -
 * invocation which may be followed by AssertJ assertions.
 *
 */
public class FirstInvocationChainAnalysisResult {
	private final MethodInvocation assertThatInvocation;
	private final ITypeBinding assertthatReturnType;

	public FirstInvocationChainAnalysisResult(MethodInvocation assertThatInvocation, ITypeBinding assertthatReturnType, ITypeBinding findFirstAssertionReturnType) {
		super();
		this.assertThatInvocation = assertThatInvocation;
		this.assertthatReturnType = assertthatReturnType;
	}

	public MethodInvocation getAssertThatInvocation() {
		return assertThatInvocation;
	}

	public ITypeBinding getAssertthatReturnType() {
		return assertthatReturnType;
	}

}
