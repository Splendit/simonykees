package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.IfStatement;

import eu.jsparrow.core.visitor.impl.GuardConditionASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link GuardConditionASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface GuardConditionEvent {

	/**
	 * 
	 * @param ifStatement
	 *            the statement to be converted into a guard statement.
	 */
	default void addMarkerEvent(IfStatement ifStatement) {
	}
}
