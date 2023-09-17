package eu.jsparrow.core.visitor.impl.inline;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Contains informations about a local variable declaration which are necessary
 * if the given variable can be in-lined.
 * 
 */
class SupportedVariableData {

	private final VariableDeclarationStatement variableDeclarationStatement;
	private final VariableDeclarationFragment variableDeclarationFragment;
	private final Expression initializer;

	static Optional<SupportedVariableData> extractVariableData(
			VariableDeclarationStatement precedingDeclarationStatement,
			String expectedIdentifier) {

		VariableDeclarationFragment uniqueDeclarationFragment = ASTNodeUtil
			.findSingletonListElement(precedingDeclarationStatement.fragments(),
					VariableDeclarationFragment.class)
			.orElse(null);

		if (uniqueDeclarationFragment == null) {
			return Optional.empty();
		}

		Expression initializer = uniqueDeclarationFragment.getInitializer();
		if (initializer == null) {
			return Optional.empty();
		}
		String identifier = uniqueDeclarationFragment.getName()
			.getIdentifier();
		if (!identifier.equals(expectedIdentifier)) {
			return Optional.empty();
		}

		return Optional.of(new SupportedVariableData(precedingDeclarationStatement, uniqueDeclarationFragment,
				initializer));
	}

	private SupportedVariableData(VariableDeclarationStatement variableDeclarationStatement,
			VariableDeclarationFragment variableDeclarationFragment, Expression initializer) {
		this.variableDeclarationStatement = variableDeclarationStatement;
		this.variableDeclarationFragment = variableDeclarationFragment;
		this.initializer = initializer;
	}

	VariableDeclarationStatement getVariableDeclarationStatement() {
		return variableDeclarationStatement;
	}

	VariableDeclarationFragment getVariableDeclarationFragment() {
		return variableDeclarationFragment;
	}

	Expression getInitializer() {
		return initializer;
	}
}
