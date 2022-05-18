package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.core.visitor.impl.BracketsToControlASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link BracketsToControlASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface BracketsToControlEvent {

	/**
	 * 
	 * @param statement
	 *            the statement to be enclosed in curly braces
	 */
	default void addMarkerEvent(Statement statement) {
	}
}
