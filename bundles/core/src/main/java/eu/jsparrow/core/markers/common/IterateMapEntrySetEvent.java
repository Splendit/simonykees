package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.EnhancedForStatement;

import eu.jsparrow.core.visitor.impl.entryset.IterateMapEntrySetASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link IterateMapEntrySetASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface IterateMapEntrySetEvent {

	/**
	 * 
	 * @param enhancedForStatement
	 *            the EnhancedForStatement iterating the key set of a
	 *            {@link java.util.Map}. to be transformed to an iteration on
	 *            the entry set of the same {@link java.util.Map}.
	 */
	default void addMarkerEvent(EnhancedForStatement enhancedForStatement) {
	}
}
