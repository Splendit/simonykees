package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public interface UseComparatorMethodsEvent {

	default void addMarkerEvent(LambdaExpression lambda, MethodInvocation lambdaReplacement) {

	}
}
