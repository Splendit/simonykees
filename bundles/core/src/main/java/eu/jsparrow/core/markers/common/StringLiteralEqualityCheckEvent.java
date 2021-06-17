package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.visitor.impl.StringLiteralEqualityCheckASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link StringLiteralEqualityCheckASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public interface StringLiteralEqualityCheckEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param stringLiteral
	 *            the string literal to be swapped.
	 * @param expression
	 *            the expression of the {@link Object#equals(Object)}
	 *            invocation.
	 */
	default void addMarkerEvent(StringLiteral stringLiteral, Expression expression) {

	}
}
