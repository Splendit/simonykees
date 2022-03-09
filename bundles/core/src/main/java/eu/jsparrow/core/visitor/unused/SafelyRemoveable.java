package eu.jsparrow.core.visitor.unused;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.rule.impl.unused.Constants;

public class SafelyRemoveable {

	private SafelyRemoveable() {
		/*
		 * Private default constructor hiding implicit public one
		 */
	}

	static Optional<ExpressionStatement> findParentStatementInBlock(Expression expression) {
		if (expression.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		ExpressionStatement expressionStatement = (ExpressionStatement) expression.getParent();
		if (expressionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}
		return Optional.of(expressionStatement);
	}

	static Optional<ExpressionStatement> isSafelyRemovable(Assignment assignment, Map<String, Boolean> options) {

		Optional<ExpressionStatement> optionalParentStatement = findParentStatementInBlock(assignment);
		if (optionalParentStatement.isPresent()) {

			boolean removeInitializersSideEffects = options.getOrDefault(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS,
					false);
			if (removeInitializersSideEffects || ExpressionWithoutSideEffectRecursive
				.isExpressionWithoutSideEffect(assignment.getRightHandSide())) {
				return optionalParentStatement;
			}
		}
		return Optional.empty();
	}

	static boolean isSafelyRemovable(VariableDeclarationFragment fragment, Map<String, Boolean> options) {
		boolean removeInitializersSideEffects = options.getOrDefault(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		if (removeInitializersSideEffects) {
			return true;
		}
		Expression initializer = fragment.getInitializer();
		return initializer == null || ExpressionWithoutSideEffectRecursive.isExpressionWithoutSideEffect(initializer);
	}
}
