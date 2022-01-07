package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachCollectASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link LambdaForEachCollectASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface LambdaForEachCollectEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            the {@code forEach} invocation to be refactored.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
