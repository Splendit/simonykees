package eu.jsparrow.core.markers.common;

import org.eclipse.jdt.core.dom.Statement;

import eu.jsparrow.core.visitor.loop.bufferedreader.BufferedReaderLinesASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link BufferedReaderLinesASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface BufferedReaderLinesEvent {

	/**
	 * 
	 * @param loop
	 *            the original loop to be replaced with BufferedReader.lines()
	 *            stream.
	 */
	default void addMarkerEvent(Statement loop) {
	}
}
