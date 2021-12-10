package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

public interface RemoveNewStringConstructorEvent {

	default void addMarkerEvent(ClassInstanceCreation node, Expression replacement) {
	}

}
