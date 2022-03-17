package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Modifier;

import eu.jsparrow.core.visitor.impl.RemoveModifiersInInterfacePropertiesASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link RemoveModifiersInInterfacePropertiesASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface RemoveModifiersInInterfacePropertiesEvent {

	/**
	 * 
	 * @param modifier
	 *            a modifier in interface properties that can be removed.
	 */
	default void addMarkerEvent(Modifier modifier) {
	}
}
