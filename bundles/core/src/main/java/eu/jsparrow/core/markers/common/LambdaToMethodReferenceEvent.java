package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

public interface LambdaToMethodReferenceEvent {

	default void addMarkerEvent(LambdaExpression lambdaExpressionNode, Expression refExpression, SimpleName name) {

	}

	default void addMarkerEvent(LambdaExpression lambdaExpressionNode, Type classInstanceCreationType) {

	}

	default void addMarkerEvent(LambdaExpression lambdaExpressionNode, Type representingType, SimpleName methodName) {

	}
}
