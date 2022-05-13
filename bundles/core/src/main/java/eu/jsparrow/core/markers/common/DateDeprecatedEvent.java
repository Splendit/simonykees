package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

public interface DateDeprecatedEvent {

	default void addMarkerEvent(ClassInstanceCreation node) {
	}
}
