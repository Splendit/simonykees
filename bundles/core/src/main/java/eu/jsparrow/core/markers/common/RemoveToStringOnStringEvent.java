package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

public interface RemoveToStringOnStringEvent {

	default void addMarkerEvent(MethodInvocation node) {
	}
}
