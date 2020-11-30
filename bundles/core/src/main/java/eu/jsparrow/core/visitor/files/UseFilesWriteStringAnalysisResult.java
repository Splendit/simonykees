package eu.jsparrow.core.visitor.files;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

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

	private final TryStatement tryStatement;

	private final Expression bufferedIOInitializer;
	private VariableDeclarationFragment fileIOResource;
	private Expression pathExpression;
	private final List<Expression> pathExpressions;
	private final Expression writeStringArgument;
	private Expression charSet;

	UseFilesWriteStringAnalysisResult(TryStatement tryStatement, MethodInvocation filesNewBufferedWriterInvocation,
			Expression pathExpression,
			Expression charSet, Expression writeStringArgument) {
		this(tryStatement, filesNewBufferedWriterInvocation, pathExpression, writeStringArgument);
		this.charSet = charSet;
	}

	UseFilesWriteStringAnalysisResult(TryStatement tryStatement, MethodInvocation filesNewBufferedWriterInvocation,
			Expression pathExpression,
			Expression writeStringArgument) {
		this.tryStatement = tryStatement;
		this.bufferedIOInitializer = filesNewBufferedWriterInvocation;
		this.pathExpression = pathExpression;
		this.pathExpressions = Collections.emptyList();
		this.writeStringArgument = writeStringArgument;
	}

	UseFilesWriteStringAnalysisResult(TryStatement tryStatement, Expression bufferedIOInitializer,
			List<Expression> pathExpressions, Expression writeStringArgument, Expression charSet,
			VariableDeclarationFragment fileIOResource) {
		this(tryStatement, bufferedIOInitializer, pathExpressions, writeStringArgument, fileIOResource);
		this.charSet = charSet;
	}

	UseFilesWriteStringAnalysisResult(TryStatement tryStatement, Expression bufferedIOInitializer,
			List<Expression> pathExpressions, Expression writeStringArgument,
			VariableDeclarationFragment fileIOResource) {
		this(tryStatement, bufferedIOInitializer, pathExpressions, writeStringArgument);
		this.fileIOResource = fileIOResource;
	}

	UseFilesWriteStringAnalysisResult(TryStatement tryStatement, Expression bufferedIOInitializer,
			List<Expression> pathExpressions, Expression writeStringArgument, Expression charSet) {
		this(tryStatement, bufferedIOInitializer, pathExpressions, writeStringArgument);
		this.charSet = charSet;
	}

	UseFilesWriteStringAnalysisResult(TryStatement tryStatement, Expression bufferedIOInitializer,
			List<Expression> pathExpressions, Expression writeStringArgument) {
		this.tryStatement = tryStatement;
		this.bufferedIOInitializer = bufferedIOInitializer;
		this.pathExpressions = pathExpressions;
		this.writeStringArgument = writeStringArgument;
	}

	TryStatement getTryStatement() {
		return tryStatement;
	}

	public Optional<VariableDeclarationFragment> getFileIOResource() {
		return Optional.ofNullable(fileIOResource);
	}

	public Optional<Expression> getPathExpression() {
		return Optional.ofNullable(pathExpression);
	}

	/**
	 * 
	 * @return if {@link #getPathExpression()} returns an empty
	 *         {@link Optional}, then this method is expected to return a
	 *         non-null list containing at least one item.
	 */
	public List<Expression> getPathExpressions() {
		return pathExpressions;
	}

	Expression getWriteStringArgument() {
		return writeStringArgument;
	}

	public Optional<Expression> getCharSet() {
		return Optional.ofNullable(charSet);
	}

	public Expression getBufferedIOInitializer() {
		return bufferedIOInitializer;
	}
}