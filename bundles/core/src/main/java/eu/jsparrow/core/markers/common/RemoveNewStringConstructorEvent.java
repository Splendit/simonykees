package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link RemoveNewStringConstructorEvent}.
 * 
 * @since 4.6.0
 */
public interface RemoveNewStringConstructorEvent {

	/**
	 * 
	 * @param node
	 *            the new String instance creation.
	 * @param replacement
	 *            the new expression serving as replacement.
	 */
	default void addMarkerEvent(ClassInstanceCreation node, Expression replacement) {
	}

}
