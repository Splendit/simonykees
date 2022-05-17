package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.FlatMapInsteadOfNestedLoopsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link FlatMapInsteadOfNestedLoopsASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface FlatMapInsteadOfNestedLoopsEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            the outer stream to be flattened.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
