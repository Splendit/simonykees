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
	private final VariableDeclarationFragment fileIOResource;
	private final List<Expression> pathExpressions;
	private final Expression charSet;

	public TransformationData(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, Expression charSet,
			VariableDeclarationFragment fileIOResource) {
		this.bufferedIOInstanceCreation = newBufferedIO;
		this.fileIOResource = fileIOResource;
		this.pathExpressions = pathExpressions;
		this.charSet = charSet;
	}

	public TransformationData(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, VariableDeclarationFragment fileIOResource) {
		this.bufferedIOInstanceCreation = newBufferedIO;
		this.fileIOResource = fileIOResource;
		this.pathExpressions = pathExpressions;
		this.charSet = null;
	}

	public TransformationData(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, Expression charSet) {
		this.bufferedIOInstanceCreation = newBufferedIO;
		this.fileIOResource = null;
		this.pathExpressions = pathExpressions;
		this.charSet = charSet;
	}

	public TransformationData(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions) {
		this.bufferedIOInstanceCreation = newBufferedIO;
		this.fileIOResource = null;
		this.pathExpressions = pathExpressions;
		this.charSet = null;
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
