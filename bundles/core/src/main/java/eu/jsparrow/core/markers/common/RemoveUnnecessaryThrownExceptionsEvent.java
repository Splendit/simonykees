package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.visitor.impl.RemoveUnnecessaryThrownExceptionsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link RemoveUnnecessaryThrownExceptionsASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface RemoveUnnecessaryThrownExceptionsEvent {

	/**
	 * 
	 * @param type
	 *            the expcetion type to be removed from the method signature.
	 */
	default void addMarkerEvent(Type type) {
	}
}
