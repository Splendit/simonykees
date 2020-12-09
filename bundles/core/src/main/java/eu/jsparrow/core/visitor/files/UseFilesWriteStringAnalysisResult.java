package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.TryStatement;
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

	private final TryStatement tryStatement;
	private final List<VariableDeclarationExpression> resourcesToRemove;

	private final List<Expression> pathExpressions;
	private final Expression writeStringArgument;
	private Expression charSet;

	UseFilesWriteStringAnalysisResult(TryStatement tryStatement, List<VariableDeclarationExpression> resourcesToRemove,
			List<Expression> pathExpressions, Expression writeStringArgument, Expression charSet) {
		this(tryStatement, resourcesToRemove, pathExpressions, writeStringArgument);
		this.charSet = charSet;
	}

	UseFilesWriteStringAnalysisResult(TryStatement tryStatement, List<VariableDeclarationExpression> resourcesToRemove,
			List<Expression> pathExpressions, Expression writeStringArgument) {
		this.tryStatement = tryStatement;
		this.resourcesToRemove = resourcesToRemove;
		this.pathExpressions = pathExpressions;
		this.writeStringArgument = writeStringArgument;
	}

	TryStatement getTryStatement() {
		return tryStatement;
	}

	public List<Expression> getPathExpressions() {
		return pathExpressions;
	}

	Expression getWriteStringArgument() {
		return writeStringArgument;
	}

	public Optional<Expression> getCharSet() {
		return Optional.ofNullable(charSet);
	}

	List<VariableDeclarationExpression> getResourcesToRemove() {
		return resourcesToRemove;
	}
}