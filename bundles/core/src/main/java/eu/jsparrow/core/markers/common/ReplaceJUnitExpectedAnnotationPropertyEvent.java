package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.NormalAnnotation;

public interface ReplaceJUnitExpectedAnnotationPropertyEvent {

	default void addMarkerEvent(NormalAnnotation normalAnnoation) {}
}
