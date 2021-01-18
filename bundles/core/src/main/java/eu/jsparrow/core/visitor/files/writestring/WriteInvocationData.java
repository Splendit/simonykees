package eu.jsparrow.core.visitor.files.writestring;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.core.visitor.files.TryResourceAnalyzer;

/**
 * Acts as a "Record" making possible to return all results of
 * {@link #findWriteInvocationData}.
 *
 */
class WriteInvocationData {
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final Expression charSequenceArgument;
	private final TryStatement tryStatement;
	private final VariableDeclarationExpression resource;
	private final Expression resourceInitializer;

	WriteInvocationData(ExpressionStatement writeInvocationStatementToReplace,
			Expression charSequenceArgument,
			TryResourceAnalyzer bufferedWriterResourceAnalyzer) {

		this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
		this.charSequenceArgument = charSequenceArgument;
		tryStatement = bufferedWriterResourceAnalyzer.getTryStatement();
		resource = bufferedWriterResourceAnalyzer.getResource();
		resourceInitializer = bufferedWriterResourceAnalyzer.getResourceInitializer();
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}

	Expression getCharSequenceArgument() {
		return charSequenceArgument;
	}

	public TryStatement getTryStatement() {
		return tryStatement;
	}

	public VariableDeclarationExpression getResource() {
		return resource;
	}

	public Expression getResourceInitializer() {
		return resourceInitializer;
	}
}