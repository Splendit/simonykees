package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.visitor.impl.ReImplementingInterfaceASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link ReImplementingInterfaceASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface ReImplementingInterfaceEvent {

	/**
	 * 
	 * @param duplicateInterface
	 *            a type declaration implementing the same interface multiple
	 *            times
	 */
	default void addMarkerEvent(Type duplicateInterface) {
	}
}
