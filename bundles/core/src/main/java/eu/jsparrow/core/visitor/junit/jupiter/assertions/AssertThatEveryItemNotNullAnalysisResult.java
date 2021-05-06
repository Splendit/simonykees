package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;

class AssertThatEveryItemNotNullAnalysisResult {
	private final Expression arrayArgument;
	private final ExpressionStatement assumeNotNullStatement;
	private final Block assumeNotNullStatementParent;

	public AssertThatEveryItemNotNullAnalysisResult(Expression arrayArgument,
			ExpressionStatement assumeNotNullStatement, Block assumeNotNullStatementParent) {
		this.arrayArgument = arrayArgument;
		this.assumeNotNullStatement = assumeNotNullStatement;
		this.assumeNotNullStatementParent = assumeNotNullStatementParent;
	}

	public Expression getArrayArgument() {
		return arrayArgument;
	}

	public ExpressionStatement getAssumeNotNullStatement() {
		return assumeNotNullStatement;
	}

	public Block getAssumeNotNullStatementParent() {
		return assumeNotNullStatementParent;
	}
}