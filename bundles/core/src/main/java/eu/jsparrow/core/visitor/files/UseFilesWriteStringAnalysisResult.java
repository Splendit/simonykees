package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

/**
 * 
 * Helper class storing informations for visitors which replace the
 * initializations of {@link java.io.BufferedReader}-objects or
 * {@link java.io.BufferedWriter}-objects by the corresponding methods of
 * {@link java.nio.file.Files}.
 * 
 * 
 * @since 3.24.0
 *
 */
class UseFilesWriteStringAnalysisResult {
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final Expression charSequenceArgument;
	private final List<VariableDeclarationExpression> resourcesToRemove;
	private final List<Expression> pathExpressions;

	private final Expression charSet;

	UseFilesWriteStringAnalysisResult(WriteMethodInvocationAnalyzer writeInvocationAnalyzer,
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer,
			List<VariableDeclarationExpression> resourcesToRemove) {
		this.writeInvocationStatementToReplace = writeInvocationAnalyzer.getWriteInvocationStatementToReplace();
		this.charSequenceArgument = writeInvocationAnalyzer.getCharSequenceArgument();
		this.resourcesToRemove = resourcesToRemove;
		this.pathExpressions = newBufferedIOArgumentsAnalyzer.getPathExpressions();
		this.charSet = newBufferedIOArgumentsAnalyzer.getCharsetExpression()
			.orElse(null);
	}

	UseFilesWriteStringAnalysisResult(WriteMethodInvocationAnalyzer writeInvocationAnalyzer,
			FileIOAnalyzer fileIOAnalyzer, List<VariableDeclarationExpression> resourcesToRemove) {
		this.writeInvocationStatementToReplace = writeInvocationAnalyzer.getWriteInvocationStatementToReplace();
		this.charSequenceArgument = writeInvocationAnalyzer.getCharSequenceArgument();
		this.resourcesToRemove = resourcesToRemove;
		this.pathExpressions = fileIOAnalyzer.getPathExpressions();
		this.charSet = fileIOAnalyzer.getCharset()
			.orElse(null);
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

	List<VariableDeclarationExpression> getResourcesToRemove() {
		return resourcesToRemove;
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}
}