package eu.jsparrow.core.visitor.files;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

public class FilesNewBufferedIOTransformationData {
	private final TryStatement tryStatement;
	private final VariableDeclarationExpression resourceToRemove;
	private final ExpressionStatement invocationStatementToReplace;
	private final List<Expression> argumentsToCopy;

	public FilesNewBufferedIOTransformationData(TryStatement tryStatement,
			VariableDeclarationExpression resourceToRemove, ExpressionStatement invocationStatementToReplace,
			List<Expression> argumentsToCopy) {
		this.tryStatement = tryStatement;
		this.resourceToRemove = resourceToRemove;
		this.invocationStatementToReplace = invocationStatementToReplace;
		this.argumentsToCopy = argumentsToCopy;
	}

	TryStatement getTryStatement() {
		return tryStatement;
	}

	VariableDeclarationExpression getResourceToRemove() {
		return resourceToRemove;
	}

	ExpressionStatement getInvocationStatementToReplace() {
		return invocationStatementToReplace;
	}

	List<Expression> getArgumentsToCopy() {
		return argumentsToCopy;
	}
}
