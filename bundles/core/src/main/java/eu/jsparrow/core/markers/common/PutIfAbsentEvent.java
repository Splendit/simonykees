package eu.jsparrow.core.markers.common;

import java.util.Map;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.PutIfAbsentASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link PutIfAbsentASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public interface PutIfAbsentEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param methodInvocation
	 *            an invocation of {@link Map#put(Object, Object)}.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
