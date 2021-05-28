package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

public interface EnumsWithoutEqualsEvent {
	
	default void addMarkerEvent(Expression replacedNode, Expression expression, Expression argument,
			InfixExpression.Operator newOperator) {}

}
