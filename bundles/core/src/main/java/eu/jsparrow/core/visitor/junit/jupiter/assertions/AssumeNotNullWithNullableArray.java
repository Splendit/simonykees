package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;

class AssumeNotNullWithNullableArray {
	private final Expression assumeNotNullArrayArgument;
	private final ExpressionStatement assumeNotNullStatement;
	private final Block assumeNotNullStatementParent;

	public AssumeNotNullWithNullableArray(Expression assumeNotNullArrayArgument,
			ExpressionStatement assumeNotNullStatement, Block assumeNotNullStatementParent) {
		this.assumeNotNullArrayArgument = assumeNotNullArrayArgument;
		this.assumeNotNullStatement = assumeNotNullStatement;
		this.assumeNotNullStatementParent = assumeNotNullStatementParent;
	}

	public Expression getAssumeNotNullArrayArgument() {
		return assumeNotNullArrayArgument;
	}

	public ExpressionStatement getAssumeNotNullStatement() {
		return assumeNotNullStatement;
	}

	public Block getAssumeNotNullStatementParent() {
		return assumeNotNullStatementParent;
	}
}