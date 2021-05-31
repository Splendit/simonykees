package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;

import eu.jsparrow.core.visitor.impl.UseIsEmptyOnCollectionsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseIsEmptyOnCollectionsASTVisitor}.
 * 
 * @since 3.31.0
 *
 */
public interface UseIsEmptyOnCollectionsEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param sizeCheck
	 *            the expression comparing the collection/map/string size to 0.
	 * @param isEmpty
	 *            the new invocation of {@code isEmpty()}.
	 */
	default void addMarkerEvent(InfixExpression sizeCheck, Expression isEmpty) {

	}

}
