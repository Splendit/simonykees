package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

public interface UseIsEmptyOnCollectionsEvent {
	
	default void addMarkerEvent(InfixExpression parent, Expression varExpression) {
		
	}

}
