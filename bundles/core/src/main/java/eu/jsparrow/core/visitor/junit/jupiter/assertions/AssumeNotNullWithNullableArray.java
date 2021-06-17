package eu.jsparrow.core.visitor.junit.jupiter.assertions;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;

/**
 * This class stores all informations to replace an invocation like
 * 
 * <pre>
 * org.junit.Assume.assumeNotNull(nullableObjectArray);
 * </pre>
 * 
 * by
 * 
 * <pre>
 * org.hamcrest.junit.MatcherAssume.assumeThat(nullableObjectArray, notNullValue());
 * org.hamcrest.junit.MatcherAssume.assumeThat(asList(nullableObjectArray), everyItem(notNullValue()));
 * </pre>
 * 
 * 
 * @since 4.0.0
 *
 */
class AssumeNotNullWithNullableArray {
	private final Expression assumeNotNullArrayArgument;
	private final ExpressionStatement assumeNotNullStatement;
	private final Block assumeNotNullStatementParent;

	AssumeNotNullWithNullableArray(Expression assumeNotNullArrayArgument,
			ExpressionStatement assumeNotNullStatement, Block assumeNotNullStatementParent) {
		this.assumeNotNullArrayArgument = assumeNotNullArrayArgument;
		this.assumeNotNullStatement = assumeNotNullStatement;
		this.assumeNotNullStatementParent = assumeNotNullStatementParent;
	}

	Expression getAssumeNotNullArrayArgument() {
		return assumeNotNullArrayArgument;
	}

	ExpressionStatement getAssumeNotNullStatement() {
		return assumeNotNullStatement;
	}

	Block getAssumeNotNullStatementParent() {
		return assumeNotNullStatementParent;
	}
}