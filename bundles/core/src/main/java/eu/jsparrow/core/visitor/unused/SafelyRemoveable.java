package eu.jsparrow.core.visitor.unused;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
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

	/**
	 * 
	 * @param referencingExpression
	 *            can represent a local variable or a field or an array access
	 *            on a local variable or a field.
	 * @param options
	 *            is needed to specify whether side effects which may be caused
	 *            for example by method invocations are relevant or not.
	 * @return an {@link Optional} storing an {@link ExpressionStatement} if the
	 *         expression statement containing the reference on the unused local
	 *         variable or field can be removed without side effect.
	 */
	static Optional<ExpressionStatement> findReferencingStatementToRemove(Expression referencingExpression,
			Map<String, Boolean> options) {
		StructuralPropertyDescriptor locationInParent = referencingExpression.getLocationInParent();
		ASTNode referencingExpressionParent = referencingExpression.getParent();
		boolean removeInitializersSideEffects = options.getOrDefault(Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		if (locationInParent == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) referencingExpressionParent;
			Optional<ExpressionStatement> optionalParentStatement = SafelyRemoveable
				.findParentStatementInBlock(assignment);
			if (optionalParentStatement.isPresent()
					&& (removeInitializersSideEffects || ExpressionWithoutSideEffectRecursive
						.isExpressionWithoutSideEffect(assignment.getRightHandSide()))) {
				return optionalParentStatement;
			}
			return Optional.empty();
		}

		if (locationInParent == PrefixExpression.OPERAND_PROPERTY) {
			return SafelyRemoveable.findParentStatementInBlock((PrefixExpression) referencingExpressionParent);
		}

		if (locationInParent == PostfixExpression.OPERAND_PROPERTY) {
			return SafelyRemoveable.findParentStatementInBlock((PostfixExpression) referencingExpressionParent);
		}

		return Optional.empty();
	}
}
