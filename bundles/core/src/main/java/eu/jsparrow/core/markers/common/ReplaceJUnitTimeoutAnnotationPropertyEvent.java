package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.NormalAnnotation;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitTimeoutAnnotationPropertyASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ReplaceJUnitTimeoutAnnotationPropertyASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface ReplaceJUnitTimeoutAnnotationPropertyEvent {

	/**
	 * 
	 * @param annotation
	 *            the {@code @Test(timeout=...)} annotation.
	 */
	default void addMarkerEvent(NormalAnnotation annotation) {
	}
}
