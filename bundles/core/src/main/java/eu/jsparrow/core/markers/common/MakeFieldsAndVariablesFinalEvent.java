package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public interface MakeFieldsAndVariablesFinalEvent {

	default void addMarkerEvent(FieldDeclaration fieldDeclaration) {
	}

	default void addMarkerEvent(VariableDeclarationStatement variableDeclarationStatement) {
	}

}
