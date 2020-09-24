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
	private final Optional<VariableDeclarationFragment> fileReaderResource;
	private final List<Expression> pathExpressions;
	private final Optional<Expression> charset;

	public TransformationData(ClassInstanceCreation newBufferedReader,
			List<Expression> pathExpressions, Optional<Expression> optionalCharset,
			VariableDeclarationFragment fileReaderResource) {
		this.bufferedIOInstanceCreation = newBufferedReader;
		this.fileReaderResource = Optional.of(fileReaderResource);
		this.pathExpressions = pathExpressions;
		this.charset = optionalCharset;
	}

	public TransformationData(ClassInstanceCreation newBufferedReader, List<Expression> pathExpressions,
			Optional<Expression> optionalCharset) {
		this.bufferedIOInstanceCreation = newBufferedReader;
		this.fileReaderResource = Optional.empty();
		this.pathExpressions = pathExpressions;
		this.charset = optionalCharset;
	}

	public Optional<VariableDeclarationFragment> getFileReaderResource() {
		return fileReaderResource;
	}

	public List<Expression> getPathExpressions() {
		return pathExpressions;
	}

	public Optional<Expression> getCharset() {
		return charset;
	}

	public ClassInstanceCreation getBufferedIOInstanceCreation() {
		return bufferedIOInstanceCreation;
	}
}
