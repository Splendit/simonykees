package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.UseListSortASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link UseListSortASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface UseListSortEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            a {@code Collections.sort()} invocation to be replaced.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
