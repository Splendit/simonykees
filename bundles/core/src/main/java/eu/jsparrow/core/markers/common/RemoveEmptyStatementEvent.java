package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.EmptyStatement;

import eu.jsparrow.core.visitor.impl.RemoveEmptyStatementASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link RemoveEmptyStatementASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface RemoveEmptyStatementEvent {

	/**
	 * 
	 * @param node
	 *            an empty statement to be removed.
	 */
	default void addMarkerEvent(EmptyStatement node) {

	}

}
