package eu.jsparrow.core.visitor.assertj;

import org.eclipse.jdt.core.dom.MethodInvocation;

public class AssertThatInvocationReplacementData {

	private final MethodInvocation assertThatInvocationToReplace;
	private final MethodInvocation chainFromPreviousStatement;

	public AssertThatInvocationReplacementData(MethodInvocation assertThatInvocationToReplace,
			MethodInvocation chainFromPreviousStatement) {
		this.assertThatInvocationToReplace = assertThatInvocationToReplace;
		this.chainFromPreviousStatement = chainFromPreviousStatement;
	}

	public MethodInvocation getAssertThatInvocationToReplace() {
		return assertThatInvocationToReplace;
	}

	public MethodInvocation getChainFromPreviousStatement() {
		return chainFromPreviousStatement;
	}
}
