package eu.jsparrow.rules.common.markers;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

public interface MarkerEventGenerator {
	
	default void addMarkerEvent(ASTNode original, ASTNode newNode) {}
	
	default void addMarkerEvent(MarkerEvent event) {}
	
	default  List<MarkerEvent> getMarkerEvents() {
		return Collections.emptyList();
	}

}
