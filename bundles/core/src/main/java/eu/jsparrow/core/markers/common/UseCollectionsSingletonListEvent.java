package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.visitor.impl.UseCollectionsSingletonListASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseCollectionsSingletonListASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public interface UseCollectionsSingletonListEvent {

	/**
	 * 
	 * @param methodName
	 *            the existing method name to be replaced.
	 * @param newMethodName
	 *            the new method name to be used instead.
	 */
	default void addMarkerEvent(SimpleName methodName, SimpleName newMethodName) {
	}

}
