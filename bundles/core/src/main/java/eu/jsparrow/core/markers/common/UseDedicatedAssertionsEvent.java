package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.junit.dedicated.UseDedicatedAssertionsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseDedicatedAssertionsASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface UseDedicatedAssertionsEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            the assertion to be replaced with the more specific
	 *            (dedicated) assertion.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
