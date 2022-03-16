package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.RemoveToStringOnStringASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link RemoveToStringOnStringASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface RemoveToStringOnStringEvent {

	/**
	 * 
	 * @param node
	 *            a toString invocation in a string object.
	 */
	default void addMarkerEvent(MethodInvocation node) {
	}
}
