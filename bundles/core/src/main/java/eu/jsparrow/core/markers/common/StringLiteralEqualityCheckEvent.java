package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

public interface StringLiteralEqualityCheckEvent {
	
	default void addMarkerEvent(StringLiteral stringLiteral, Expression expression) {
		
	}
}
