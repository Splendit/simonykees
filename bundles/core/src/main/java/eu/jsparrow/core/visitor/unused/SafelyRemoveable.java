package eu.jsparrow.core.visitor.unused;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.rule.impl.unused.Constants;

/**
 * Offers methods to find {@link ExpressionStatement} nodes which can be removed
 * together with the reference on an unused variable or field without causing
 * side effects.
 * 
 * @since 4.9.0
 *
 */
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
			if (removeInitializersSideEffects) {
				return optionalParentStatement;
			}
			if (ExpressionWithoutSideEffectRecursive
				.isExpressionWithoutSideEffect(assignment.getRightHandSide())
					&& ExpressionWithoutSideEffectRecursive
						.isExpressionWithoutSideEffect(assignment.getLeftHandSide())) {
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

	/**
	 * 
	 * @param expression
	 *            can represent a local variable or a field or an array access
	 *            on a local variable or a field.
	 * @param options
	 *            is needed to specify whether side effects which may be caused
	 *            for example by method invocations are relevant or not.
	 * @return an {@link Optional} storing an {@link ExpressionStatement}
	 *         representing an assignment statement or an increment statement or
	 *         a decrement statement without any relevant side effect. As soon
	 *         as any relevant side effect is found in the corresponding
	 *         expression, an empty {@link Optional} is returned.
	 * 
	 */
	static Optional<ExpressionStatement> findSafelyRemovableReassignment(Expression expression,
			Map<String, Boolean> options) {

		if (expression.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) expression.getParent();
			return SafelyRemoveable.isSafelyRemovable(assignment, options);
		}

		boolean removeInitializersSideEffects = options.getOrDefault(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		if (removeInitializersSideEffects
				|| ExpressionWithoutSideEffectRecursive.isExpressionWithoutSideEffect(expression)) {

			if (expression.getLocationInParent() == PrefixExpression.OPERAND_PROPERTY) {
				return SafelyRemoveable.findParentStatementInBlock((PrefixExpression) expression.getParent());
			}

			if (expression.getLocationInParent() == PostfixExpression.OPERAND_PROPERTY) {
				return SafelyRemoveable.findParentStatementInBlock((PostfixExpression) expression.getParent());
			}
		}

		return Optional.empty();
	}

}
