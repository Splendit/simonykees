package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.security.random.ReuseRandomObjectsASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ReuseRandomObjectsASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface ReuseRandomObjectsEvent {

	/**
	 * 
	 * @param statement
	 *            the declaration of a local random object generator.
	 */
	default void addMarkerEvent(VariableDeclarationStatement statement) {
	}
}
