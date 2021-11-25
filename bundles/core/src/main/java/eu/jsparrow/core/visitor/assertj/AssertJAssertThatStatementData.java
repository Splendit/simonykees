package eu.jsparrow.core.visitor.assertj;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

class AssertJAssertThatStatementData {

	private final MethodInvocation assertThatInvocation;
	private final Expression assertThatArgument;
	private final List<MethodInvocation> chainFollowingAssertThat;
	private final ExpressionStatement assertThatStatement;

	AssertJAssertThatStatementData(MethodInvocation assertThatInvocation, Expression assertThatArgument,
			List<MethodInvocation> chainFollowingAssertThat, ExpressionStatement assertThatStatement) {
		this.assertThatInvocation = assertThatInvocation;
		this.assertThatArgument = assertThatArgument;
		this.chainFollowingAssertThat = chainFollowingAssertThat;
		this.assertThatStatement = assertThatStatement;
	}

	Expression getAssertThatArgument() {
		return assertThatArgument;
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
