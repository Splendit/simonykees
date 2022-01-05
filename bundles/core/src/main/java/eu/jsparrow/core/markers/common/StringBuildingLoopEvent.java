package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.EnhancedForStatement;

public interface StringBuildingLoopEvent {

	default void addMarkerEvent(EnhancedForStatement forStatement) {
	}
}
