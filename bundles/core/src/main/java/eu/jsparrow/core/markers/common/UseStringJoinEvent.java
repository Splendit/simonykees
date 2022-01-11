package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.impl.UseStringJoinASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseStringJoinASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface UseStringJoinEvent {

	/**
	 * 
	 * @param parentMethod
	 *            the original method to be replaced i.e.,
	 *            {@code collect(Collectors.joining()}.
	 * @param collection
	 *            the collection whose values are concatenated.
	 * @param joinArguments
	 *            the delimiter.
	 */
	default void addMarkerEvent(MethodInvocation parentMethod, Expression collection, List<Expression> joinArguments) {
	}

}
