package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.impl.inline.InlineLocalVariablesASTVisitor;

/**
 * An interface to add {@link InlineLocalVariablesEvent}s events for
 * {@link InlineLocalVariablesASTVisitor}.
 * 
 * @since 4.19.0
 *
 */
public interface InlineLocalVariablesEvent {

	/**
	 * 
	 * @param fragment
	 *            the original variable declaration fragment to be removed by
	 *            in-lining.
	 */
	default void addMarkerEvent(VariableDeclarationFragment fragment) {
	}
}
