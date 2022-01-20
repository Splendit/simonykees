package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachMapASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link LambdaForEachMapASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface LambdaForEachMapEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            the invocation of the {@code forEach} method to be refactored.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
