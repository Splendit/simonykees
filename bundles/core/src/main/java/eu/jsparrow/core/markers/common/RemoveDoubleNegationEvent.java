package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.PrefixExpression;

import eu.jsparrow.core.visitor.impl.RemoveDoubleNegationASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link RemoveDoubleNegationASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface RemoveDoubleNegationEvent {

	/**
	 * 
	 * @param prefixExpression
	 *            the prefix expression with multiple negations to be reduced.
	 */
	default void addMarkerEvent(PrefixExpression prefixExpression) {
	}
}
