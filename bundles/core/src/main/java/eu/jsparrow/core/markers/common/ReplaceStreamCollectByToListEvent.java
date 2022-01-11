package eu.jsparrow.core.markers.common;

import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.stream.tolist.ReplaceStreamCollectByToListASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ReplaceStreamCollectByToListASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface ReplaceStreamCollectByToListEvent {

	/**
	 * 
	 * @param supportedStreamCollectInvocation
	 *            a invocation of
	 *            {@link Stream#collect(java.util.stream.Collector)} that can be
	 *            replaced by {@docRoot toList()}.
	 */
	default void addMarkerEvent(MethodInvocation supportedStreamCollectInvocation) {

	}
}
