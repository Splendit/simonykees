package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public interface UseArraysStreamEvent {

	default void addMarkerEvent(MethodInvocation parent, List<Expression> arguments) {
	}

	default void addMarkerEvent(MethodInvocation parent, List<Expression> arguments, String name,
			Expression experssion) {
	}
}
