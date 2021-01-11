package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

/**
 * Stores all informations in connection with the replacement of invocation
 * statements calling {@link java.io.Writer#write(String)} on a resource which
 * is initialized by the invocation of a constructor of
 * {@link java.io.BufferedWriter}.
 * 
 * 
 * @since 3.24.0
 *
 */
class WriteReplacementUsingBufferedWriterConstructor {
	private final List<VariableDeclarationExpression> resourcesToRemove;
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final Expression charSequenceArgument;
	private final List<Expression> pathExpressions;
	private final Expression charSet;

	WriteReplacementUsingBufferedWriterConstructor(List<VariableDeclarationExpression> resourcesToRemove,
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer) {
		this.resourcesToRemove = resourcesToRemove;
		this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
		this.charSequenceArgument = charSequenceArgument;
		this.pathExpressions = newBufferedIOArgumentsAnalyzer.getPathExpressions();
		this.charSet = newBufferedIOArgumentsAnalyzer.getCharsetExpression()
			.orElse(null);
	}

	WriteReplacementUsingBufferedWriterConstructor(List<VariableDeclarationExpression> resourcesToRemove,
			ExpressionStatement writeInvocationStatementToReplace, Expression charSequenceArgument,
			FileIOAnalyzer fileIOAnalyzer) {
		this.resourcesToRemove = resourcesToRemove;
		this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
		this.charSequenceArgument = charSequenceArgument;
		this.pathExpressions = fileIOAnalyzer.getPathExpressions();
		this.charSet = fileIOAnalyzer.getCharset()
			.orElse(null);
	}

	List<VariableDeclarationExpression> getResourcesToRemove() {
		return resourcesToRemove;
	}

	public List<Expression> getPathExpressions() {
		return pathExpressions;
	}

	Expression getCharSequenceArgument() {
		return charSequenceArgument;
	}

	public Optional<Expression> getCharSet() {
		return Optional.ofNullable(charSet);
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}
}