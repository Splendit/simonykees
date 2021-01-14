package eu.jsparrow.core.visitor.files.writestring;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

/**
 * Stores all informations in connection with the replacement of invocation
 * statements calling {@link java.io.Writer#write(String)} on a resource which
 * is initialized by <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.charset.Charset, java.nio.file.OpenOption...)}
 * or <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.file.OpenOption...)}.
 * 
 * @since 3.24.0
 * 
 */
class WriteReplacementUsingFilesNewBufferedWriter {
	private final VariableDeclarationExpression resourceToRemove;
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final List<Expression> argumentsToCopy;

	WriteReplacementUsingFilesNewBufferedWriter(
			VariableDeclarationExpression resourceToRemove,
			ExpressionStatement writeInvocationStatementToReplace,
			List<Expression> argumentsToCopy) {
		this.resourceToRemove = resourceToRemove;
		this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
		this.argumentsToCopy = argumentsToCopy;
	}

	VariableDeclarationExpression getResourceToRemove() {
		return resourceToRemove;
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}

	List<Expression> getArgumentsToCopy() {
		return argumentsToCopy;
	}
}
