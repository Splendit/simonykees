package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

import eu.jsparrow.core.visitor.impl.IndexOfToContainsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link IndexOfToContainsASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public interface IndexOfToContainsEvent {

	/**
	 * 
	 * @param expression
	 *            The infix expression to be replaced.
	 * @param methodExpression
	 *            the expression of the new {@code contains} invocation.
	 * @param methodArgument
	 *            the parameter of the new {@code contains} invocation.
	 */
	default void addMarkerEvent(InfixExpression expression, Expression methodExpression, Expression methodArgument) {
	}

	/**
	 * 
	 * @param expression
	 *            The infix expression to be replaced.
	 * @param methodExpression
	 *            the expression of the new {@code contains} invocation.
	 * @param methodArgument
	 *            the parameter of the new {@code contains} invocation.
	 * @param operator
	 *            the negator operator when checking for not contains.
	 */
	default void addMarkerEvent(InfixExpression expression, Expression methodExpression, Expression methodArgument,
			PrefixExpression.Operator operator) {
	}
}
