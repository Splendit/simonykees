package eu.jsparrow.core.markers.common;

import java.security.SecureRandom;

import org.eclipse.jdt.core.dom.Expression;

import eu.jsparrow.core.visitor.security.random.UseSecureRandomASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseSecureRandomASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface UseSecureRandomEvent {

	/**
	 * 
	 * @param expression
	 *            the initialization of a random object that should be replaced
	 *            by {@link SecureRandom}.
	 */
	default void addMarkerEvent(Expression expression) {
	}
}
