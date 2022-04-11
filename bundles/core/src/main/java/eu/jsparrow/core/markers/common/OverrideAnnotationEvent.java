package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public interface OverrideAnnotationEvent {

	default void addMarkerEvent(MethodDeclaration methodDeclaration) {
	}
}
