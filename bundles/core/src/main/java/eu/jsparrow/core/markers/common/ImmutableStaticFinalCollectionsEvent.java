package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.impl.ImmutableStaticFinalCollectionsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ImmutableStaticFinalCollectionsASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface ImmutableStaticFinalCollectionsEvent {

	/**
	 * 
	 * @param fragment
	 *            the collection declaration
	 */
	default void addMarkerEvent(VariableDeclarationFragment fragment) {
	}
}
