package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import eu.jsparrow.core.visitor.impl.RemoveUnusedParameterASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link RemoveUnusedParameterASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public interface RemoveUnusedParameterEvent {

	/**
	 * 
	 * @param parameter
	 *            the parameter to be dropped.
	 * @param methodDeclaration
	 *            the method declaration whose parameter is about to be removed.
	 * @param parameterIndex
	 *            the index of the parameter to remove.
	 */
	default void addMarkerEvent(SingleVariableDeclaration parameter, MethodDeclaration methodDeclaration,
			int parameterIndex) {
	}
}
