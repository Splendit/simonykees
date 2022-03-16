package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.ReplaceStringFormatByFormattedASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link ReplaceStringFormatByFormattedASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface ReplaceStringFormatByFormattedEvent {

	/**
	 * 
	 * @param invocation
	 *            a {@code String.format} invocation to be replaced.
	 */
	default void addMarkerEvent(MethodInvocation invocation) {
	}
}
