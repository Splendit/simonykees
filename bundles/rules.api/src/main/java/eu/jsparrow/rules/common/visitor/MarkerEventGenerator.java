package eu.jsparrow.rules.common.visitor;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.rules.common.MarkerEvent;

public interface MarkerEventGenerator {
	
	default void addMarkerEvent(ASTNode original, ASTNode newNode) {}
	
	default void addMarkerEvent(MarkerEvent event) {}
	
	default  List<MarkerEvent> getMarkerEvents() {
		return Collections.emptyList();
	}

}
