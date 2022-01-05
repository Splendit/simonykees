package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.TryStatement;

public interface MultiCatchEvent {


	default void addMarkerEvent(TryStatement node) {
	}
}
