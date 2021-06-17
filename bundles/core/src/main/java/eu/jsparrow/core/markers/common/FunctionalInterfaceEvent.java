package eu.jsparrow.core.markers.common;

import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link FunctionalInterfaceASTVisitor}.
 * 
 * @since 4.0.0
 *
 */
public interface FunctionalInterfaceEvent {

	/**
	 * Creates the code example for the marker preview. Creates an instance of
	 * {@link RefactoringEventImplt}s and records it as
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param classInstanceCreation
	 *            the original node to be replaced by a lambda.
	 * @param parameters
	 *            the parameters of the new lambda expression.
	 * @param block
	 *            the body of the new lambda expression.
	 */
	default void addMarkerEvent(ClassInstanceCreation classInstanceCreation, List<SingleVariableDeclaration> parameters,
			Block block) {
	}

}
