package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import eu.jsparrow.core.visitor.impl.OverrideAnnotationRuleASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link OverrideAnnotationRuleASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface OverrideAnnotationEvent {

	/**
	 * 
	 * @param methodDeclaration
	 *            the method declaration to be annotated.
	 */
	default void addMarkerEvent(MethodDeclaration methodDeclaration) {
	}
}
