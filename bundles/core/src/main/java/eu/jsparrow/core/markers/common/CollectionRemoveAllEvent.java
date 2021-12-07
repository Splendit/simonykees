package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.CollectionRemoveAllASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link CollectionRemoveAllASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public interface CollectionRemoveAllEvent {

	/**
	 * 
	 * @param node
	 *            the method invocation to be replaced.
	 */
	default void addMarkerEvent(MethodInvocation node) {
	}

}
