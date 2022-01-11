package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.EnhancedForStatement;

import eu.jsparrow.core.visitor.loop.stream.AbstractEnhancedForLoopToStreamASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for implementations of
 * {@link AbstractEnhancedForLoopToStreamASTVisitor} visitor. Includes
 * transformations of {@link EnhancedForStatement} to stream forEach, anyMatch,
 * findFirst, sum, takeWhile, etc.
 * 
 * @since 4.7.0
 *
 */
public interface EnhancedForLoopToStreamEvent {

	/**
	 * 
	 * @param enhancedFor
	 *            the original node to be replaced with a stream.
	 */
	default void addMarkerEvent(EnhancedForStatement enhancedFor) {

	}
}
