package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.StringLiteral;

public interface StringFormatLineSeparatorEvent {
	
	default void addMarkerEvent(StringLiteral stringLiteral) {}

}
