package eu.jsparrow.core.visitor.unused;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Assignment;
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

	static Optional<ExpressionStatement> isSafelyRemovable(Assignment assignment, Map<String, Boolean> options) {
		if (assignment.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		Expression rightHandSide = assignment.getRightHandSide();

		ExpressionStatement expressionStatement = (ExpressionStatement) assignment.getParent();
		boolean ignoreSideEffects = options.getOrDefault(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		if (ignoreSideEffects) {
			return Optional.of(expressionStatement);
		}

		boolean safelyRemovable = ExpressionWithoutSideEffectRecursive.isExpressionWithoutSideEffect(rightHandSide);
		if (safelyRemovable) {
			return Optional.of(expressionStatement);
		}
		return Optional.empty();
	}

	static boolean isSafelyRemovable(VariableDeclarationFragment fragment, Map<String, Boolean> options) {
		boolean ignoreSideEffects = options.getOrDefault(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		if (ignoreSideEffects) {
			return true;
		}
		Expression initializer = fragment.getInitializer();
		return initializer == null || ExpressionWithoutSideEffectRecursive.isExpressionWithoutSideEffect(initializer);
	}
}
