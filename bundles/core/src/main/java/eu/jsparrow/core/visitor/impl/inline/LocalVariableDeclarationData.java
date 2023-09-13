package eu.jsparrow.core.visitor.impl.inline;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Contains all informations about a local variable declaration which are
 * necessary if the given variable can be in-lined.
 * 
 */
class LocalVariableDeclarationData {

	private final VariableDeclarationStatement variableDeclarationStatement;
	private final VariableDeclarationFragment variableDeclarationFragment;
	private final Expression initializer;

	static Optional<LocalVariableDeclarationData> findData(Block block, String expectedIdentifier) {
		@SuppressWarnings("rawtypes")
		List statements = block.statements();
		for (Object statement : statements) {
			if (statement instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) statement;
				List<VariableDeclarationFragment> variableDeclarationFragments = ASTNodeUtil
					.convertToTypedList(variableDeclarationStatement.fragments(), VariableDeclarationFragment.class);
				for (VariableDeclarationFragment fragment : variableDeclarationFragments) {
					Expression initializer = fragment.getInitializer();
					if (initializer != null) {
						SimpleName name = fragment.getName();
						String identifier = name.getIdentifier();
						if (identifier.equals(expectedIdentifier)) {
							LocalVariableDeclarationData declarationData = new LocalVariableDeclarationData(
									variableDeclarationStatement, fragment, initializer);
							return Optional.of(declarationData);
						}
					}
				}
			}
		}
		return Optional.empty();
	}

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
