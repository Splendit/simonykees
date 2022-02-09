package eu.jsparrow.core.markers.common;

import java.io.BufferedWriter;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.TryStatement;

import eu.jsparrow.core.visitor.files.writestring.UseFilesWriteStringASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseFilesWriteStringASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface UseFilesWriteStringEvent {

	/**
	 * 
	 * @param tryStatement
	 *            the original {@link TryStatement} creating the
	 *            {@link BufferedWriter}s
	 */
	default void addMarkerEvent(TryStatement tryStatement) {
	}

	/**
	 * 
	 * @param expressionStatement
	 *            invocation of a {@link BufferedWriter#write(String)}
	 */
	default void addMarkerEvent(ExpressionStatement expressionStatement) {
	}
}
