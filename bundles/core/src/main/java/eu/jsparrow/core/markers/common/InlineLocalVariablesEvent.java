package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;

import eu.jsparrow.core.visitor.impl.inline.InlineLocalVariablesASTVisitor;

/**
 * An interface to add {@link InlineLocalVariablesEvent}s events for
 * {@link InlineLocalVariablesASTVisitor}.
 * 
 * @since 4.19.0
 *
 */
public interface InlineLocalVariablesEvent {

	default void addMarkerEvent(ReturnStatement returnStatement) {
	}

	default void addMarkerEvent(ThrowStatement throwStatement) {
	}

}
