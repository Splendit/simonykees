package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.IfStatement;

public interface GuardConditionEvent {

	default void addMarkerEvent(IfStatement ifStatement) {
	}
}
