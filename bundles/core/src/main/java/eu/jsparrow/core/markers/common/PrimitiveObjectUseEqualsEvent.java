package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.InfixExpression;

public interface PrimitiveObjectUseEqualsEvent {

	default void addMarkerEvent(InfixExpression infixExpression) {
	}
}
