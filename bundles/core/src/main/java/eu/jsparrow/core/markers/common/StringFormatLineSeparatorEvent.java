package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.core.visitor.impl.StringFormatLineSeparatorASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link StringFormatLineSeparatorASTVisitor}.
 * 
 * @since 4.11.0
 *
 */
public interface StringFormatLineSeparatorEvent {

	/**
	 * 
	 * @param stringLiteral
	 *            the string literal containing '\n' or '\r\n'.
	 */
	default void addMarkerEvent(StringLiteral stringLiteral) {
	}
}
