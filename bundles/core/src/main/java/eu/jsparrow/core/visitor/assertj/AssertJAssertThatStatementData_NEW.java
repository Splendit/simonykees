package eu.jsparrow.core.visitor.assertj;

import java.util.List;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

class AssertJAssertThatStatementData_NEW {

	private final MethodInvocation assertThatInvocation;
	private final String assertThatArgumentIdentifier;
	private final List<MethodInvocation> chainFollowingAssertThat;
	private final ExpressionStatement assertThatStatement;

	AssertJAssertThatStatementData_NEW(MethodInvocation assertThatInvocation,
			String assertThatArgumentIdentifier,
			List<MethodInvocation> chainFollowingAssertThat, ExpressionStatement assertThatStatement) {
		this.assertThatInvocation = assertThatInvocation;
		this.assertThatArgumentIdentifier = assertThatArgumentIdentifier;
		this.chainFollowingAssertThat = chainFollowingAssertThat;
		this.assertThatStatement = assertThatStatement;
	}

	String getAssertThatArgumentIdentifier() {
		return assertThatArgumentIdentifier;
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
