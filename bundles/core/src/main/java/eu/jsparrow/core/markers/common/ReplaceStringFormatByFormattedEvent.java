package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

public interface ReplaceStringFormatByFormattedEvent {

	default void addMarkerEvent(MethodInvocation invocation) {
	}
}
