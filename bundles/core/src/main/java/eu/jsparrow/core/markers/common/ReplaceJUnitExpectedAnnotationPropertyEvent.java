package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.NormalAnnotation;

import eu.jsparrow.core.visitor.junit.ReplaceJUnitExpectedAnnotationPropertyASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ReplaceJUnitExpectedAnnotationPropertyASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface ReplaceJUnitExpectedAnnotationPropertyEvent {

	/**
	 * 
	 * @param normalAnnoation
	 *            the {@code @Test(expected=....)} annotation.
	 */
	default void addMarkerEvent(NormalAnnotation normalAnnoation) {
	}
}
