package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * 
 * @since 4.7.0
 *
 */
public interface UseStringJoinEvent {
	
	default void addMarkerEvent(MethodInvocation parentMethod, Expression collection, List<Expression> joinArguments) {
	}

}
