package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.impl.StringBufferToBuilderASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link StringBufferToBuilderASTVisitor}.
 * 
 * @since 4.10.0
 *
 */
public interface StringBufferToBuilderEvent {

	/**
	 * 
	 * @param declaration
	 *            the buffer declaration to be replaced by a builder.
	 */
	default void addMarkerEvent(VariableDeclarationStatement declaration) {
	}
}
