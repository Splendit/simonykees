package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.comparatormethods.UseComparatorMethodsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseComparatorMethodsASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public interface UseComparatorMethodsEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param lambda
	 *            the lambda expression representing a comparator.
	 * @param lambdaReplacement
	 *            the new invocation of the predefined comparator method.
	 */
	default void addMarkerEvent(LambdaExpression lambda, MethodInvocation lambdaReplacement) {

	}
}
