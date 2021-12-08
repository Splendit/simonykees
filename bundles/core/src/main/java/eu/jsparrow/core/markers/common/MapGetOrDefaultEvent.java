package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

public interface MapGetOrDefaultEvent {

	default void addMarkerEvent(MethodInvocation methodInvocation, Expression key, Expression defaultValue) {
	}
}
