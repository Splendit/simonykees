package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.SimpleName;

public interface UseCollectionsSingletonListEvent {
	
	default void addMarkerEvent(SimpleName methodName, SimpleName newMethodName) {
	}

}
