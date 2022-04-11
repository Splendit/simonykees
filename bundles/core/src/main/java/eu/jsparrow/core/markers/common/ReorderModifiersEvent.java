package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

public interface ReorderModifiersEvent {

	default void addMarkerEvent(List<Modifier> modifiers) {
	}
}
