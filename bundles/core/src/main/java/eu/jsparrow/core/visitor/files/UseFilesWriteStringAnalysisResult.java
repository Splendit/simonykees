package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
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

	private final ClassInstanceCreation bufferedIOInstanceCreation;
	private VariableDeclarationFragment fileIOResource;
	private final List<Expression> pathExpressions;
	private final Expression writeStringArgument;
	private Expression charSet;

	UseFilesWriteStringAnalysisResult(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, Expression writeStringArgument, Expression charSet,
			VariableDeclarationFragment fileIOResource) {
		this(newBufferedIO, pathExpressions, writeStringArgument, fileIOResource);
		this.charSet = charSet;
	}

	UseFilesWriteStringAnalysisResult(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, Expression writeStringArgument,
			VariableDeclarationFragment fileIOResource) {
		this(newBufferedIO, pathExpressions, writeStringArgument);
		this.fileIOResource = fileIOResource;

	}

	UseFilesWriteStringAnalysisResult(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, Expression writeStringArgument, Expression charSet) {
		this(newBufferedIO, pathExpressions, writeStringArgument);
		this.charSet = charSet;
	}

	UseFilesWriteStringAnalysisResult(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, Expression writeStringArgument) {
		this.bufferedIOInstanceCreation = newBufferedIO;
		this.pathExpressions = pathExpressions;
		this.writeStringArgument = writeStringArgument;
	}

	public Optional<VariableDeclarationFragment> getFileIOResource() {
		return Optional.ofNullable(fileIOResource);
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

	public ClassInstanceCreation getBufferedIOInstanceCreation() {
		return bufferedIOInstanceCreation;
	}
}