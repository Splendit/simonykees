package eu.jsparrow.core.visitor.assertj;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

class AssertJAssertThatStatementData {

	private final MethodInvocation assertThatInvocation;
	private final String assertThatArgumentIdentifier;
	private final MethodInvocation completeInvocationChain;
	private final ExpressionStatement assertThatStatement;

	public AssertJAssertThatStatementData(MethodInvocation assertThatInvocation, String assertThatArgumentIdentifier,
			MethodInvocation completeInvocationChain, ExpressionStatement assertThatStatement) {
		this.assertThatInvocation = assertThatInvocation;
		this.assertThatArgumentIdentifier = assertThatArgumentIdentifier;
		this.completeInvocationChain = completeInvocationChain;
		this.assertThatStatement = assertThatStatement;
	}

	public String getAssertThatArgumentIdentifier() {
		return assertThatArgumentIdentifier;
	}

	public ExpressionStatement getAssertThatStatement() {
		return assertThatStatement;
	}

	public MethodInvocation getAssertThatInvocation() {
		return assertThatInvocation;
	}

	public MethodInvocation getCompleteInvocationChain() {
		return completeInvocationChain;
	}

}
