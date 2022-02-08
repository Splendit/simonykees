package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

public interface UseFilesBufferedIOMethodsEvent {

	default void addMarkerEvent(ClassInstanceCreation bufferedIOInstanceCreation) {

	}
}
