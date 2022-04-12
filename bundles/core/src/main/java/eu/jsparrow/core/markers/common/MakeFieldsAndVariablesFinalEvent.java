package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.make_final.MakeFieldsAndVariablesFinalASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link MakeFieldsAndVariablesFinalASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface MakeFieldsAndVariablesFinalEvent {

	/**
	 * 
	 * @param fieldDeclaration
	 *            a field declaration to be converted to a constant.
	 */
	default void addMarkerEvent(FieldDeclaration fieldDeclaration) {
	}

	/**
	 * 
	 * @param variableDeclarationStatement
	 *            a variable declaration to be converted to a constant.
	 */
	default void addMarkerEvent(VariableDeclarationStatement variableDeclarationStatement) {
	}

}
