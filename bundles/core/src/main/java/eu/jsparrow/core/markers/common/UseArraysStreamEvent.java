package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.UseArraysStreamASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseArraysStreamASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface UseArraysStreamEvent {

	/**
	 * 
	 * @param parent
	 *            the original method to be refactored.
	 * @param arguments
	 *            the elements to be included in the stream.
	 */
	default void addMarkerEvent(MethodInvocation parent, List<Expression> arguments) {
	}

	/**
	 * 
	 * @param parent
	 *            the original method to be refactored.
	 * @param arguments
	 *            the elements to be included in the stream.
	 * @param name
	 *            the type of the array to create the stream from
	 * @param experssion
	 *            the name of the expression to generate the stream from.
	 */
	default void addMarkerEvent(MethodInvocation parent, List<Expression> arguments, String name,
			Expression experssion) {
	}
}
