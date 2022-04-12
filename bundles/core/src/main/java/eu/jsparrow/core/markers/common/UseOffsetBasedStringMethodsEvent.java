package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.UseOffsetBasedStringMethodsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseOffsetBasedStringMethodsASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface UseOffsetBasedStringMethodsEvent {

	/**
	 * 
	 * @param node
	 *            the method invocation to be simplified.
	 */
	default void addMarkerEvent(MethodInvocation node) {
	}
}
