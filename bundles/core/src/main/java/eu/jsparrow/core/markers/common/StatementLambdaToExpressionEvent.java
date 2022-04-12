package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.LambdaExpression;

import eu.jsparrow.core.visitor.impl.StatementLambdaToExpressionASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link StatementLambdaToExpressionASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface StatementLambdaToExpressionEvent {

	/**
	 * 
	 * @param lambdaExpression
	 *            the lambda expression to be simplified.
	 */
	default void addMarkerEvent(LambdaExpression lambdaExpression) {
	}
}
