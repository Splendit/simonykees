package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.EmptyStatement;

public interface RemoveEmptyStatementEvent {
	
	default void addMarkerEvent(EmptyStatement node) {
		
	}

}
