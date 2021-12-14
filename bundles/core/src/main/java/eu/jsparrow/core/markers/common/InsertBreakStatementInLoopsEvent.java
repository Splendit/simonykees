package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;

import eu.jsparrow.core.visitor.impl.InsertBreakStatementInLoopsASTVisitor;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link InsertBreakStatementInLoopsASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public interface InsertBreakStatementInLoopsEvent {

	/**
	 * 
	 * @param forStatement
	 *            the existing loop.
	 * @param ifStatement
	 *            the single if statement in the loop body.
	 * @param ifBodyBlock
	 *            the body of the if statement.
	 */
	default void addMarkerEvent(EnhancedForStatement forStatement, IfStatement ifStatement, Block ifBodyBlock) {
	}

	/**
	 * 
	 * @param forStatement
	 *            the existing loop.
	 * @param ifStatement
	 *            the single if statement in the loop body.
	 * @param expressionStatement
	 *            the body of the if statement.
	 */
	default void addMarkerEvent(EnhancedForStatement forStatement, IfStatement ifStatement,
			ExpressionStatement expressionStatement) {
	}
}
