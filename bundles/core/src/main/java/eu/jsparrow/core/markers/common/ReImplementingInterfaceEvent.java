package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Type;

public interface ReImplementingInterfaceEvent {

	default void addMarkerEvent(Type duplicateInterface) {
	}
}
