package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;

import eu.jsparrow.core.visitor.impl.AvoidConcatenationInLoggingStatementsASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link AvoidConcatenationInLoggingStatementsASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public interface AvoidConcatenationInLoggingStatementsEvent {

	/**
	 * 
	 * @param infixExpression
	 *            the original infix expression to be replaced
	 * @param newNode
	 *            the new expression with placeholders for arguments
	 */
	default void addMarkerEvent(InfixExpression infixExpression, ASTNode newNode) {
	}

}
