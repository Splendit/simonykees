package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

public interface InefficientConstructorEvent {

	default void addMarkerEvent(Expression refactorCandidateParameter, MethodInvocation node,
			Expression replaceParameter) {
	}

	default void addMarkerEvent(ClassInstanceCreation node, SimpleName refactorPrimitiveType,
			Expression refactorCandidateParameter) {
		
	}
}
