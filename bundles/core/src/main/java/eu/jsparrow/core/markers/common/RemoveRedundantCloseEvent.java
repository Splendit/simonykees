package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ExpressionStatement;

public interface RemoveRedundantCloseEvent {

	default void addMarkerEvent(ExpressionStatement closeStatement) {
	}

}
