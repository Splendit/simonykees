package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

public interface IndexOfToContainsEvent {

	default void addMarkerEvent(InfixExpression expression, Expression methodExpression, Expression methodArgument) {
	}

	default void addMarkerEvent(InfixExpression expression, Expression methodExpression, Expression methodArgument, PrefixExpression.Operator operator) {
	}
}
