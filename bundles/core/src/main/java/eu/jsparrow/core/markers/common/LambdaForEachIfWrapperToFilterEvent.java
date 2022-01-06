package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;

public interface LambdaForEachIfWrapperToFilterEvent {

	default void addMarkerEvent(MethodInvocation methodInvocationNode, Expression ifExpression,
			VariableDeclaration parameterDeclaration) {
	}
}
