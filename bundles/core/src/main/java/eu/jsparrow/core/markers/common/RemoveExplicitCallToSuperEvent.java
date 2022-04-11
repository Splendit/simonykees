package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

public interface RemoveExplicitCallToSuperEvent {

	default void addMarkerEvent(SuperConstructorInvocation node) {
	}
}
