package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

public interface RemoveUnusedParameterEvent {

	default void addMarkerEvent(SingleVariableDeclaration parameter, MethodDeclaration methodDeclaration,
			int parameterIndex) {
	}
}
