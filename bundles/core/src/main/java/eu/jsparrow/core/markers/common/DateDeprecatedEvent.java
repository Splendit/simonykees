package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import eu.jsparrow.core.visitor.impl.DateDeprecatedASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link DateDeprecatedASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface DateDeprecatedEvent {

	/**
	 * 
	 * @param node
	 *            the deprecated constructor
	 */
	default void addMarkerEvent(ClassInstanceCreation node) {
	}
}
