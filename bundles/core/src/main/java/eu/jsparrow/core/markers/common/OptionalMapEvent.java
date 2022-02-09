package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.LambdaExpression;

import eu.jsparrow.core.visitor.optional.OptionalMapASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link OptionalMapASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface OptionalMapEvent {
	
	/**
	 * 
	 * @param lambdaExpression
	 *            the consumer of a
	 *            {@link Optional#ifPresent(java.util.function.Consumer)}
	 */
	default void addMarkerEvent(LambdaExpression lambdaExpression) {
	}
}
