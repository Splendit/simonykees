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
	private final Optional<VariableDeclarationFragment> fileIOResource;
	private final List<Expression> pathExpressions;
	private final Optional<Expression> charSet;

	public TransformationData(ClassInstanceCreation newBufferedIO,
			List<Expression> pathExpressions, Optional<Expression> optionalCharSet,
			VariableDeclarationFragment fileIOResource) {
		this.bufferedIOInstanceCreation = newBufferedIO;
		this.fileIOResource = Optional.of(fileIOResource);
		this.pathExpressions = pathExpressions;
		this.charSet = optionalCharSet;
	}

	public TransformationData(ClassInstanceCreation newBufferedIO, List<Expression> pathExpressions,
			Optional<Expression> optionalCharSet) {
		this.bufferedIOInstanceCreation = newBufferedIO;
		this.fileIOResource = Optional.empty();
		this.pathExpressions = pathExpressions;
		this.charSet = optionalCharSet;
	}

	public Optional<VariableDeclarationFragment> getFileIOResource() {
		return fileIOResource;
	}

	public List<Expression> getPathExpressions() {
		return pathExpressions;
	}

	public Optional<Expression> getCharSet() {
		return charSet;
	}

	public ClassInstanceCreation getBufferedIOInstanceCreation() {
		return bufferedIOInstanceCreation;
	}
}
