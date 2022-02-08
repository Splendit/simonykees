package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public interface ReuseRandomObjectsEvent {

	default void addMarkerEvent(VariableDeclarationStatement statement) {}
}
