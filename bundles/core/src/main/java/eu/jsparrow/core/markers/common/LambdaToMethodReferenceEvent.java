package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeMethodReference;

import eu.jsparrow.core.visitor.impl.LambdaToMethodReferenceASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link LambdaToMethodReferenceASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public interface LambdaToMethodReferenceEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param lambdaExpression
	 *            the lambda to be replaced.
	 * @param refExpression
	 *            the expression of the new {@link ExpressionMethodReference}.
	 * @param name
	 *            the name of the new {@link ExpressionMethodReference}.
	 */
	default void addMarkerEvent(LambdaExpression lambdaExpression, Expression refExpression, SimpleName name) {

	}

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param lambdaExpression
	 *            the lambda to be replaced.
	 * @param classInstanceCreationType
	 *            the type of the new {@link CreationReference. }
	 */
	default void addMarkerEvent(LambdaExpression lambdaExpression, Type classInstanceCreationType) {

	}

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param lambdaExpression
	 *            the lambda to be replaced.
	 * @param representingType
	 *            the type of the new {@link TypeMethodReference}.
	 * @param methodName
	 *            the name of the new {@link TypeMethodReference}.
	 */
	default void addMarkerEvent(LambdaExpression lambdaExpression, Type representingType, SimpleName methodName) {

	}
}
