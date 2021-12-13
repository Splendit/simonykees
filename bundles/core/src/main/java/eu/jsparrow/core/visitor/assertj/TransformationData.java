package eu.jsparrow.core.visitor.assertj;

import java.util.List;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Wrapper class storing all information that is necessary for the replacement
 * of subsequent {@code assertThat} - invocation chains by one single invocation
 * chains containing all assertions.
 * 
 * @since 4.6.0
 *
 */
class TransformationData {
	private final ExpressionStatement firstAssertThatStatement;
	private final List<ExpressionStatement> assertJAssertThatStatementsToRemove;
	private final MethodInvocation assertThatInvocation;
	private final List<MethodInvocation> invocationChainElementList;

	TransformationData(ExpressionStatement firstAssertThatStatement,
			List<ExpressionStatement> assertJAssertThatStatementsToRemove, MethodInvocation assertThatInvocation,
			List<MethodInvocation> invocationChainElementList) {
		this.firstAssertThatStatement = firstAssertThatStatement;
		this.assertJAssertThatStatementsToRemove = assertJAssertThatStatementsToRemove;
		this.assertThatInvocation = assertThatInvocation;
		this.invocationChainElementList = invocationChainElementList;
	}

	ExpressionStatement getFirstAssertThatStatement() {
		return firstAssertThatStatement;
	}

	List<ExpressionStatement> getAssertJAssertThatStatementsToRemove() {
		return assertJAssertThatStatementsToRemove;
	}

	MethodInvocation getAssertThatInvocation() {
		return assertThatInvocation;
	}

	List<MethodInvocation> getInvocationChainElementList() {
		return invocationChainElementList;
	}
}
