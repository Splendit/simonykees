package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ExpressionStatement;

public interface RemoveCollectionAddAllEvent {

	default void addMarkerEvent(ExpressionStatement expressionStatement) {
	}
}
