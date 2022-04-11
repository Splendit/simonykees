package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.LambdaExpression;

public interface StatementLambdaToExpressionEvent {

	default void addMarkerEvent(LambdaExpression lambdaExpression) {
	}
}
