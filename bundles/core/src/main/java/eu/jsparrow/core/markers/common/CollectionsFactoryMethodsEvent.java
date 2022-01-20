package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.factory.methods.CollectionsFactoryMethodsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link CollectionsFactoryMethodsASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public interface CollectionsFactoryMethodsEvent {

	/**
	 * 
	 * @param methodInvocation
	 *            the original method invocation to be replaced by a Collections
	 *            factory method
	 * @param expressionTypeName
	 *            the collection where the factory method is defined
	 * @param factoryMethodName
	 *            the name of the factory method. Either {@code of} or
	 *            {@code ofEntries}.
	 * @param elements
	 *            the elements to be included in the collection.
	 */
	default void addMarkerEvent(MethodInvocation methodInvocation, String expressionTypeName, String factoryMethodName,
			List<Expression> elements) {

	}
}
