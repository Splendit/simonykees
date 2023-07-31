package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.impl.InlineLocalVariablesASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link InlineLocalVariablesASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface InlineLocalVariablesEvent {

	/**
	 * 
	 * @param ifStatement
	 *            the original if statement to be collapsed with the inner if
	 *            statement.
	 */
	default void addMarkerEvent(VariableDeclarationFragment fragment) {
	}
}
