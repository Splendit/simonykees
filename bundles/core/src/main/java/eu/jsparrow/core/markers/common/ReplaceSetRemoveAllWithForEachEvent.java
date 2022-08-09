package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.ReplaceSetRemoveAllWithForEachASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link ReplaceSetRemoveAllWithForEachASTVisitor}.
 * 
 * @since 4.13.0
 *
 */
public interface ReplaceSetRemoveAllWithForEachEvent {

	/**
	 * 
	 * @param invocation
	 *            a {@code set.removeAll} invocation to be replaced.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
