package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.core.visitor.impl.ReplaceWrongClassForLoggerASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s events for
 * {@link ReplaceWrongClassForLoggerASTVisitor}.
 * 
 * @since 4.13.0
 *
 */
public interface ReplaceWrongClassForLoggerEvent {

	/**
	 * 
	 * @param typeLiteral
	 *            a {@link TypeLiteral} to be replaced.
	 */
	default void addMarkerEvent(TypeLiteral typeLiteral) {

	}

}
