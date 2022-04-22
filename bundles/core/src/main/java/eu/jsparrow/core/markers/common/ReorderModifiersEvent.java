package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Modifier;

import eu.jsparrow.core.visitor.impl.ReorderModifiersASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ReorderModifiersASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface ReorderModifiersEvent {

	/**
	 * 
	 * @param modifiers
	 *            the list of modifiers to be sorted.
	 */
	default void addMarkerEvent(List<Modifier> modifiers) {
	}
}
