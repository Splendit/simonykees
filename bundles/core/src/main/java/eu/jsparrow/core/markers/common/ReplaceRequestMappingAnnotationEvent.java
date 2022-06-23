package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.NormalAnnotation;

import eu.jsparrow.core.visitor.spring.ReplaceRequestMappingAnnotationASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ReplaceRequestMappingAnnotationASTVisitor}.
 * 
 * @since 4.12.0
 *
 */
public interface ReplaceRequestMappingAnnotationEvent {

	/**
	 * 
	 * @param node
	 *            the annotation invocation to be replaced.
	 */
	default void addMarkerEvent(NormalAnnotation node) {
	}
}
