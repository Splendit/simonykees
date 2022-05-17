package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitExpectedExceptionASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ReplaceJUnitExpectedExceptionASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface ReplaceJUnitExpectedExceptionEvent {

	/**
	 * 
	 * @param node
	 *            the node responsible for throwing the expected exception.
	 *            E.g., a method invocation, a new instance declaration, or a
	 *            throw statement.
	 */
	default void addMarkerEvent(ASTNode node) {
	}
}
