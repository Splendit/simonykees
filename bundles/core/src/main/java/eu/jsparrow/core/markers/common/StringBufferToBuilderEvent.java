package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public interface StringBufferToBuilderEvent {

	default void addMarkerEvent(VariableDeclarationStatement declaration) {
	}
}
