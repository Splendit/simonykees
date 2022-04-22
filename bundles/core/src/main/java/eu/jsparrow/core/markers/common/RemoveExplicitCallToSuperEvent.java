package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import eu.jsparrow.core.visitor.impl.RemoveExplicitCallToSuperASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link RemoveExplicitCallToSuperASTVisitor}
 * 
 * @since 4.10.0
 *
 */
public interface RemoveExplicitCallToSuperEvent {

	/**
	 * 
	 * @param node
	 *            the super() invocation to be removed.
	 */
	default void addMarkerEvent(SuperConstructorInvocation node) {
	}
}
