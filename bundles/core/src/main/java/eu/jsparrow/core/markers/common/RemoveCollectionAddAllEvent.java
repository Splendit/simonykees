package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ExpressionStatement;

import eu.jsparrow.core.visitor.impl.RemoveCollectionAddAllASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link RemoveCollectionAddAllASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface RemoveCollectionAddAllEvent {

	/**
	 * 
	 * @param expressionStatement
	 *            the expression statement to be removed
	 */
	default void addMarkerEvent(ExpressionStatement expressionStatement) {
	}
}
