package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

public interface RemoveNullCheckBeforeInstanceofEvent {
	
	default void addMarkerEvent(Expression leftOperand, InfixExpression infixExpression, Expression expression) {
		
	}
}
