package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.impl.MultiVariableDeclarationLineASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link MultiVariableDeclarationLineASTVisitor}.
 * 
 * @since 4.9.0
 *
 */
public interface MultiVariableDeclarationLineEvent {

	/**
	 * 
	 * @param declaration
	 *            either a {@link FieldDeclaration} or a
	 *            {@link VariableDeclarationStatement} declaring multiple
	 *            fragments.
	 */
	default void addMarkerEvent(ASTNode declaration) {
	}
}
