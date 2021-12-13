package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.visitor.impl.DiamondOperatorASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link DiamondOperatorASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public interface DiamondOperatorEvent {

	/**
	 * 
	 * @param parameterizedType
	 *            the parameterized type to be modified
	 * @param rhsTypeArguments
	 *            the list of explicit type parameters to be removed.
	 */
	default void addMarkerEvent(ParameterizedType parameterizedType, List<Type> rhsTypeArguments) {
	}

}
