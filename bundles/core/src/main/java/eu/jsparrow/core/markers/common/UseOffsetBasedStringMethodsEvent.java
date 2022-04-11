package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

public interface UseOffsetBasedStringMethodsEvent {

	default void addMarkerEvent(MethodInvocation node) {
	}
}
