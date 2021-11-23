package eu.jsparrow.core.visitor.assertj;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;

class TransformationData {
	private final Block block;
	private final ExpressionStatement firstAssertThatStatement;
	private final List<ExpressionStatement> assertJAssertThatStatementsToRemove;
	private final MethodInvocation assertThatInvocation;
	private final List<InvocationChainElement> invocationChainElementList;

	TransformationData(Block block, ExpressionStatement firstAssertThatStatement,
			List<ExpressionStatement> assertJAssertThatStatementsToRemove, MethodInvocation assertThatInvocation,
			List<InvocationChainElement> invocationChainElementList) {
		this.block = block;
		this.firstAssertThatStatement = firstAssertThatStatement;
		this.assertJAssertThatStatementsToRemove = assertJAssertThatStatementsToRemove;
		this.assertThatInvocation = assertThatInvocation;
		this.invocationChainElementList = invocationChainElementList;
	}

	Block getBlock() {
		return block;
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

	List<InvocationChainElement> getInvocationChainElementList() {
		return invocationChainElementList;
	}		
}
