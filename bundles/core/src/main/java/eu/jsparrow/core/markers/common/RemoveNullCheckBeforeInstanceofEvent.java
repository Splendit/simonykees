package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;

import eu.jsparrow.core.visitor.impl.RemoveNullCheckBeforeInstanceofASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link RemoveNullCheckBeforeInstanceofASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public interface RemoveNullCheckBeforeInstanceofEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param nullCheck
	 *            the null check to be removed.
	 * @param infixExpression
	 *            the infix expression containing the null check on the left
	 *            operand.
	 * @param expression
	 *            expecting the {@link InstanceofExpression}.
	 */
	default void addMarkerEvent(Expression nullCheck, InfixExpression infixExpression, Expression expression) {

	}
}
