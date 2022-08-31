package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import org.eclipse.jdt.core.dom.IfStatement;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ReplaceMultiBranchIfBySwitchASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface ReplaceMultiBranchIfBySwitchEvent {

	/**
	 * Creates an instance of {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param ifStatement
	 *            the original if statement to be replaced by a switch
	 *            expression.
	 */
	default void addMarkerEvent(IfStatement ifStatement) {
	}

}
