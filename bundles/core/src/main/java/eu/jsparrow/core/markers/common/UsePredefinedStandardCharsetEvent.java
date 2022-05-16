package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.OverrideAnnotationRuleASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link OverrideAnnotationRuleASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface UsePredefinedStandardCharsetEvent {

	/**
	 * 
	 * @param MethodInvocation
	 *            the method invocation to be replaced by the qualified name of
	 *            the char set constant
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation) {
	}
}
