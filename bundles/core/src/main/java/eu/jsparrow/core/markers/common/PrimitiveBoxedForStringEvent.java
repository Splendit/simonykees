package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.visitor.impl.PrimitiveBoxedForStringASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link PrimitiveBoxedForStringASTVisitor}.
 * 
 * @since 3.31.0
 *
 */
public interface PrimitiveBoxedForStringEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param node
	 *            the node to be replaced.
	 * @param expression
	 *            the argument of the new method.
	 * @param name
	 *            the name of the new method.
	 * @param primitiveType
	 *            the boxed primitive type name.
	 */
	default void addMarkerEvent(ASTNode node, Expression expression, SimpleName name,
			SimpleName primitiveType) {
	}

}
