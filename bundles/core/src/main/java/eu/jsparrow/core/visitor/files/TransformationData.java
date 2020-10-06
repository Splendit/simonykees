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
 * @since 3.22.0
 *
 */
class TransformationData {

	private final ClassInstanceCreation bufferedIOInstanceCreation;
	private final List<Expression> pathExpressions;
	private VariableDeclarationFragment fileIOResource;
	private Expression charSet;

	public TransformationData(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, Expression charSet,
			VariableDeclarationFragment fileIOResource) {
		this(newBufferedIO, pathExpressions, fileIOResource);
		this.charSet = charSet;
	}

	public TransformationData(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, VariableDeclarationFragment fileIOResource) {
		this(newBufferedIO, pathExpressions);
		this.fileIOResource = fileIOResource;

	}

	public TransformationData(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, Expression charSet) {
		this(newBufferedIO, pathExpressions);
		this.charSet = charSet;
	}

	public TransformationData(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions) {
		this.bufferedIOInstanceCreation = newBufferedIO;
		this.pathExpressions = pathExpressions;
	}

	public Optional<VariableDeclarationFragment> getFileIOResource() {
		return Optional.ofNullable(fileIOResource);
	}

	public List<Expression> getPathExpressions() {
		return pathExpressions;
	}

	public Optional<Expression> getCharSet() {
		return Optional.ofNullable(charSet);
	}

	public ClassInstanceCreation getBufferedIOInstanceCreation() {
		return bufferedIOInstanceCreation;
	}
}
