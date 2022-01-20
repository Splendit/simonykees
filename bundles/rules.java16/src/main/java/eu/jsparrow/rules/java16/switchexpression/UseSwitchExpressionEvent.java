package eu.jsparrow.rules.java16.switchexpression;

import org.eclipse.jdt.core.dom.SwitchStatement;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseSwitchExpressionASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface UseSwitchExpressionEvent {

	/**
	 * Creates an instance of {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param switchStatement
	 *            the original switch statement to be replaced by a switch
	 *            expression.
	 */
	default void addMarkerEvent(SwitchStatement switchStatement) {
	}

}
