package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.InfixExpression;

import eu.jsparrow.core.visitor.impl.PrimitiveObjectUseEqualsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link PrimitiveObjectUseEqualsASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface PrimitiveObjectUseEqualsEvent {

	/**
	 * 
	 * @param infixExpression
	 *            the infix expression to be replaced by an equals invocation.
	 */
	default void addMarkerEvent(InfixExpression infixExpression) {
	}
}
