package eu.jsparrow.core.visitor.impl.inline;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * Contains all informations about a local variable declaration which are
 * necessary if the given variable can be in-lined.
 * 
 */
class LocalVariableDeclarationData {

	private final VariableDeclarationStatement variableDeclarationStatement;
	private final VariableDeclarationFragment variableDeclarationFragment;
	private final Expression initializer;

	LocalVariableDeclarationData(VariableDeclarationStatement variableDeclarationStatement,
			VariableDeclarationFragment variableDeclarationFragment, Expression initializer) {
		this.variableDeclarationStatement = variableDeclarationStatement;
		this.variableDeclarationFragment = variableDeclarationFragment;
		this.initializer = initializer;
	}

	public VariableDeclarationStatement getVariableDeclarationStatement() {
		return variableDeclarationStatement;
	}

	public VariableDeclarationFragment getVariableDeclarationFragment() {
		return variableDeclarationFragment;
	}

	public Expression getInitializer() {
		return initializer;
	}
}
