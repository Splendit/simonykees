package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.IfStatement;

import eu.jsparrow.core.visitor.impl.CollapseIfStatementsASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link CollapseIfStatementsASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface CollapseIfStatementsEvent {

	/**
	 * 
	 * @param ifStatement
	 *            the original if statement to be collapsed with the inner if
	 *            statement.
	 */
	default void addMarkerEvent(IfStatement ifStatement) {
	}
}
