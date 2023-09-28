package eu.jsparrow.core.markers.common;

import java.util.Map;

import org.eclipse.jdt.core.dom.IfStatement;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

public interface UseTernaryOperatorEvent {
	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param methodInvocation
	 *            an invocation of {@link Map#put(Object, Object)}.
	 */
	default void addMarkerEvent(IfStatement ifStatement) {
	}
}
