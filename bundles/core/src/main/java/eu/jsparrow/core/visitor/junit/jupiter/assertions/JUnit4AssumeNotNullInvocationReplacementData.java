package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;

class JUnit4AssumeNotNullInvocationReplacementData extends JUnit4MethodInvocationReplacementData {

	private final ExpressionStatement parentExpressionStatement;
	private final Block blockOfExpressionStatement;
	private final Supplier<ExpressionStatement> assertThatListElementsNotNull;

	JUnit4AssumeNotNullInvocationReplacementData(
			JUnit4MethodInvocationReplacementData invocationReplecementData,
			ExpressionStatement parentExpressionStatement,
			Block blockOfExpressionStatement,
			Supplier<ExpressionStatement> assertThatListElementsNotNull) {
		super(invocationReplecementData);
		this.parentExpressionStatement = parentExpressionStatement;
		this.blockOfExpressionStatement = blockOfExpressionStatement;
		this.assertThatListElementsNotNull = assertThatListElementsNotNull;
	}

	public ExpressionStatement getParentExpressionStatement() {
		return parentExpressionStatement;
	}

	public Block getBlockOfExpressionStatement() {
		return blockOfExpressionStatement;
	}

	public Supplier<ExpressionStatement> getAssertThatListElementsNotNull() {
		return assertThatListElementsNotNull;
	}
}