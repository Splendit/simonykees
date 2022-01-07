package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.core.visitor.loop.whiletoforeach.WhileToForEachASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link ForToForEachASTVisitor} and {@link WhileToForEachASTVisitor}.
 * 
 * @param <T>
 *            the type of the loop statement. Either {@link ForStatement} or
 *            {@link WhileStatement}.
 * 
 * @since 4.7.0
 */
public interface LoopToForEachEvent<T> {

	/**
	 * 
	 * @param loop
	 *            the original node to be replaced with
	 *            {@link EnhancedForStatementa}.
	 * @param iterableNode
	 *            the name of the iterable being interated.
	 * @param iteratorDecl
	 *            the name of the loop variable.
	 */
	default void addMarkerEvent(T loop, SimpleName iterableNode,
			SingleVariableDeclaration iteratorDecl) {
	}

}
