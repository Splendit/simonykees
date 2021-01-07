package eu.jsparrow.core.visitor.files;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

/**
 * Stores all Data needed necessary for a transformation of code in connection
 * with invocations of <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.charset.Charset, java.nio.file.OpenOption...)}
 * or <br>
 * {@link java.nio.file.Files#newBufferedWriter(java.nio.file.Path, java.nio.file.OpenOption...)}.
 *
 */
class TransformationDataUsingFilesNewBufferedWriter {
	private final VariableDeclarationExpression resourceToRemove;
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final List<Expression> argumentsToCopy;

	TransformationDataUsingFilesNewBufferedWriter(
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
