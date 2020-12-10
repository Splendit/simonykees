package eu.jsparrow.core.visitor.files;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

public class FilesNewBufferedIOTransformationData {
	private final TryStatement tryStatement;
	private final VariableDeclarationExpression resourceToRemove;
	private final ExpressionStatement writeInvocationStatementToReplace;
	private final List<Expression> argumentsToCopy;

	public FilesNewBufferedIOTransformationData(TryStatement tryStatement,
			VariableDeclarationExpression resourceToRemove, ExpressionStatement writeInvocationStatementToReplace,
			List<Expression> argumentsToCopy) {
		this.tryStatement = tryStatement;
		this.resourceToRemove = resourceToRemove;
		this.writeInvocationStatementToReplace = writeInvocationStatementToReplace;
		this.argumentsToCopy = argumentsToCopy;
	}

	TryStatement getTryStatement() {
		return tryStatement;
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
