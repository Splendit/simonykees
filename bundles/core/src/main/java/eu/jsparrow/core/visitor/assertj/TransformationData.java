package eu.jsparrow.core.visitor.assertj;

import java.util.List;

import org.eclipse.jdt.core.dom.ExpressionStatement;

class TransformationData {
	private final List<AssertThatInvocationReplacementData> assertThatInvocationReplacememnts;
	private final List<ExpressionStatement> assertThatStatementsToRemove;

	public TransformationData(List<AssertThatInvocationReplacementData> assertThatInvocationReplacememnts,
			List<ExpressionStatement> assertThatStatementsToRemove) {
		this.assertThatInvocationReplacememnts = assertThatInvocationReplacememnts;
		this.assertThatStatementsToRemove = assertThatStatementsToRemove;
	}

	public List<AssertThatInvocationReplacementData> getAssertThatInvocationReplacememnts() {
		return assertThatInvocationReplacememnts;
	}

	public List<ExpressionStatement> getAssertThatStatementsToRemove() {
		return assertThatStatementsToRemove;
	}
}
