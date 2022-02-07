package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Assignment;

public interface ArithmeticAssignmentEvent {

	default void addMarkerEvent(Assignment node) {
	}
}
