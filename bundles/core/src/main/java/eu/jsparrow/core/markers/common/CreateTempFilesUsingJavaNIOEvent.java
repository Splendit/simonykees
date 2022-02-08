package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

public interface CreateTempFilesUsingJavaNIOEvent {

	default void addMarkerEvent(MethodInvocation replacedCreateTempFileInvocation) {
	}
}
