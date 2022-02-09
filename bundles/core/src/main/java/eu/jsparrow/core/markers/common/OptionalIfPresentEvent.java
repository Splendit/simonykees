package eu.jsparrow.core.markers.common;

import java.util.Optional;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.optional.OptionalIfPresentASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link OptionalIfPresentASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface OptionalIfPresentEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            the invocation of {@link Optional#isPresent()}
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
