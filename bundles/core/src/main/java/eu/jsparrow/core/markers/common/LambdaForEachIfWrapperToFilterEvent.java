package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachIfWrapperToFilterASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link LambdaForEachIfWrapperToFilterASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface LambdaForEachIfWrapperToFilterEvent {

	/**
	 * 
	 * @param methodInvocationNode
	 *            the original {@code forEach} invocation.
	 * @param ifExpression
	 *            the expression of the {@link IfStatement} to be moved to a
	 *            {@code filter} invocation
	 * @param parameterDeclaration
	 *            the parameter of the lambda expression to be used in the
	 *            {@code filter} invocation.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocationNode, Expression ifExpression,
			VariableDeclaration parameterDeclaration) {
	}
}
