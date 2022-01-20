package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.TryStatement;

import eu.jsparrow.core.visitor.impl.trycatch.MultiCatchASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link MultiCatchASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface MultiCatchEvent {

	/**
	 * @param node
	 *            the original try-catch statement whose catch clauses shall be
	 *            collapsed
	 */
	default void addMarkerEvent(TryStatement node) {
	}
}
