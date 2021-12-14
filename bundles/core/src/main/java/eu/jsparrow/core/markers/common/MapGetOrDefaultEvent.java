package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.MapGetOrDefaultASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link MapGetOrDefaultASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public interface MapGetOrDefaultEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            the existing invocation of {@code get}
	 * @param key
	 *            the key to extract values from the map
	 * @param defaultValue
	 *            the default value
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation, Expression key, Expression defaultValue) {
	}
}
