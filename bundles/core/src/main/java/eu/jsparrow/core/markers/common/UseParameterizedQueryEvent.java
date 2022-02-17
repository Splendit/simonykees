package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.security.AbstractDynamicQueryASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link AbstractDynamicQueryASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface UseParameterizedQueryEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            the invocation of a dynamic query.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {

	}
}
