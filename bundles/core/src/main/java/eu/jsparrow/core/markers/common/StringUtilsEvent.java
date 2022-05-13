package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

public interface StringUtilsEvent {
	
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
