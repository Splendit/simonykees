package eu.jsparrow.core.visitor.assertj;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodInvocation;

class AssertJAssertThatInvocationChainData {
	private final MethodInvocation assertThatInvocation;
	private final List<InvocationChainElement> subsequentChainElements;

	AssertJAssertThatInvocationChainData(MethodInvocation assertThatInvocation,
			List<InvocationChainElement> subsequentChainElements) {
		this.assertThatInvocation = assertThatInvocation;
		this.subsequentChainElements = subsequentChainElements;
	}

	MethodInvocation getAssertThatInvocation() {
		return assertThatInvocation;
	}

	List<InvocationChainElement> getSubsequentChainElements() {
		return subsequentChainElements;
	}
}
