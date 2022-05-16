package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.StringUtilsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link StringUtilsASTVisitor}.
 * 
 * 
 * @since 4.11.0
 *
 */
public interface StringUtilsEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            the invocation to be replaced with the equivalent one defined
	 *            in Apache Commons Lang.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
