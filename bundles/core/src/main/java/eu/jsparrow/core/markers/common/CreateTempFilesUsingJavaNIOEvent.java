package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.security.CreateTempFilesUsingJavaNIOASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link CreateTempFilesUsingJavaNIOASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface CreateTempFilesUsingJavaNIOEvent {

	/**
	 * 
	 * @param replacedCreateTempFileInvocation
	 *            the original invocation of {@code File.createTempFile()}
	 */
	default void addMarkerEvent(MethodInvocation replacedCreateTempFileInvocation) {
	}
}
