package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Assignment;

import eu.jsparrow.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ArithmethicAssignmentASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface ArithmeticAssignmentEvent {

	/**
	 * 
	 * @param node
	 *            the original assignment to be replaced.
	 */
	default void addMarkerEvent(Assignment node) {
	}
}
