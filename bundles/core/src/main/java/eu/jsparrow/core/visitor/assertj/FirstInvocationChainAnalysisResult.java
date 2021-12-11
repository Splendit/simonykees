package eu.jsparrow.core.visitor.assertj;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Wrapper class storing additional informations about a successful analysis of
 * the first invocation chain beginning with an {@code assertThat} - invocation.
 * The values stored by this class are needed for the analysis of subsequent
 * invocation chains in order to find out whether or not two or more invocation
 * chains beginning with an {@code assertThat} - invocation can be chained
 * together to one single invocation chain.
 *
 * @since 4.6.0
 */
public class FirstInvocationChainAnalysisResult {
	private final MethodInvocation assertThatInvocation;
	private final ITypeBinding firstAssertionReturnType;

	public FirstInvocationChainAnalysisResult(MethodInvocation assertThatInvocation,
			ITypeBinding firstAssertionReturnType) {
		super();
		this.assertThatInvocation = assertThatInvocation;
		this.firstAssertionReturnType = firstAssertionReturnType;
	}

	public MethodInvocation getAssertThatInvocation() {
		return assertThatInvocation;
	}

	public ITypeBinding getFirstAssertionReturnType() {
		return firstAssertionReturnType;
	}
}
