package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.PrefixExpression;

public interface RemoveDoubleNegationEvent {

	default void addMarkerEvent(PrefixExpression prefixExpression) {
	}
}
