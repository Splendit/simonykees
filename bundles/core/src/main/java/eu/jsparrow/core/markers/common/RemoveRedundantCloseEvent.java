package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ExpressionStatement;

import eu.jsparrow.core.visitor.impl.trycatch.close.RemoveRedundantCloseASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link RemoveRedundantCloseASTVisitor}.
 * 
 * 4.11.0
 *
 */
public interface RemoveRedundantCloseEvent {

	/**
	 * 
	 * @param closeStatement
	 *            the statement representing a {@code resource.close()}
	 *            invocation.
	 */
	default void addMarkerEvent(ExpressionStatement closeStatement) {
	}

}
