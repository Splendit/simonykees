package eu.jsparrow.rules.java16.textblock;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.TextBlock;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseTextBlockASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface UseTextBlockEvent {

	/**
	 * Creates an instance of {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param node
	 *            the original infix expression to be replaced.
	 * @param newNode
	 *            the new text block
	 */
	default void addMarkerEvent(InfixExpression node, TextBlock newNode) {
	}
}