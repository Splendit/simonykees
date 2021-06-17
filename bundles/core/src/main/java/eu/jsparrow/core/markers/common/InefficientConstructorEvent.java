package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.visitor.impl.InefficientConstructorASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link InefficientConstructorASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public interface InefficientConstructorEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param original
	 *            the String argument {@code "true"} or {@code "false"} of
	 *            {@link Boolean#valueOf(String)} invocation.
	 * @param booleanValueOf
	 *            an invocation of {@link Boolean#valueOf(String)}
	 * @param booleanLiteral
	 *            a boolean literal. Either {@code true} or {@code false}.
	 */
	default void addMarkerEvent(Expression original, MethodInvocation booleanValueOf,
			Expression booleanLiteral) {
	}

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param node
	 *            the original node to be replaced
	 * @param boxedType
	 *            the expression of the new {@code BoxedType.valueOf(...)}
	 *            invocation.
	 * @param parameter
	 *            the parameter of the {@code BoxedType.valueOf(...)}
	 *            invocation.
	 */
	default void addMarkerEvent(ClassInstanceCreation node, SimpleName boxedType, Expression parameter) {

	}
}
