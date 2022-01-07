package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.EnhancedForStatement;

import eu.jsparrow.core.visitor.impl.StringBuildingLoopASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link StringBuildingLoopASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface StringBuildingLoopEvent {

	/**
	 * 
	 * @param forStatement
	 *            the original loop to be refactored.
	 */
	default void addMarkerEvent(EnhancedForStatement forStatement) {
	}
}
