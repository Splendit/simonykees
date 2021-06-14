package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

import eu.jsparrow.core.visitor.impl.EnumsWithoutEqualsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link EnumsWithoutEqualsASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public interface EnumsWithoutEqualsEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param replacedNode
	 *            the original node being replaced, i.e., an invocation of
	 *            {@link Object#equals(Object)}.
	 * @param expression
	 *            the expression of {@link Object#equals(Object)} invocation.
	 * @param argument
	 *            the argument of {@link Object#equals(Object)} invocation
	 * @param newOperator
	 *            either {@link InfixExpression.Operator#EQUALS} or
	 *            {@link InfixExpression.Operator#NOT_EQUALS}.
	 */
	default void addMarkerEvent(Expression replacedNode, Expression expression, Expression argument,
			InfixExpression.Operator newOperator) {
	}

}
