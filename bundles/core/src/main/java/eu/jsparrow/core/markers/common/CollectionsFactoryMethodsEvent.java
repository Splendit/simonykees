package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public interface CollectionsFactoryMethodsEvent {

	default void addMarkerEvent(MethodInvocation methodInvocation, String expressionTypeName, String factoryMethodName,
			List<Expression> elements) {

	}
}
