package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.core.visitor.impl.RemoveRedundantTypeCastASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link RemoveRedundantTypeCastASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public interface RemoveRedundantTypeCastEvent {

	/**
	 * 
	 * @param nodeToBeReplaced
	 *            the casting expression to be replaced.
	 * @param replacement
	 *            the new replacement.
	 */
	default void addMarkerEvent(ASTNode nodeToBeReplaced, ASTNode replacement) {
	}
}
