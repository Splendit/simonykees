package eu.jsparrow.core.visitor.assertj;

import java.util.List;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

class AssertJAssertThatStatementData {

	private final MethodInvocation assertThatInvocation;
	private final List<MethodInvocation> chainFollowingAssertThat;
	private final ExpressionStatement assertThatStatement;

	AssertJAssertThatStatementData(MethodInvocation assertThatInvocation,
			List<MethodInvocation> chainFollowingAssertThat, ExpressionStatement assertThatStatement) {
		this.assertThatInvocation = assertThatInvocation;
		this.chainFollowingAssertThat = chainFollowingAssertThat;
		this.assertThatStatement = assertThatStatement;
	}

	ExpressionStatement getAssertThatStatement() {
		return assertThatStatement;
	}

	MethodInvocation getAssertThatInvocation() {
		return assertThatInvocation;
	}

	List<MethodInvocation> getChainFollowingAssertThat() {
		return chainFollowingAssertThat;
	}
}
