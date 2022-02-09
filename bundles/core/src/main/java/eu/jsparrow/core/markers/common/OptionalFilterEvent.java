package eu.jsparrow.core.markers.common;

import java.util.Optional;

import org.eclipse.jdt.core.dom.LambdaExpression;

import eu.jsparrow.core.visitor.optional.OptionalFilterASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link OptionalFilterASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface OptionalFilterEvent {

	/**
	 * 
	 * @param lambdaExpression
	 *            the consumer of a
	 *            {@link Optional#ifPresent(java.util.function.Consumer)}
	 */
	default void addMarkerEvent(LambdaExpression lambdaExpression) {
	}

}
