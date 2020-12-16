package eu.jsparrow.core.visitor.files;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;

/**
 * Stores all Data needed necessary for a transformation of code in connection
 * with invocations of <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.charset.Charset, java.nio.file.OpenOption...)}
 * or <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.file.OpenOption...)}.
 *
 */
public class FilesNewBufferedIOTransformationData {
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final List<Expression> argumentsToCopy;

	public FilesNewBufferedIOTransformationData(
			ExpressionStatement writeInvocationStatementToReplace,
			List<Expression> argumentsToCopy) {
		this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
		this.argumentsToCopy = argumentsToCopy;
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}

	List<Expression> getArgumentsToCopy() {
		return argumentsToCopy;
	}
}
