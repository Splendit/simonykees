package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.TryStatement;

import eu.jsparrow.core.visitor.impl.trycatch.TryWithResourceASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link TryWithResourceASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface TryWithResourceEvent {

	/**
	 * 
	 * @param node the original try-catch statement to be refactored. 
	 */
	default void addMarkerEvent(TryStatement node) {
	}
}
