package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ASTNode;

public interface MultiVariableDeclarationLineEvent {

	default void addMarkerEvent(ASTNode declaration) {		
	}
}
