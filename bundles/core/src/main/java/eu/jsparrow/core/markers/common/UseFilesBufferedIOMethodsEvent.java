package eu.jsparrow.core.markers.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import eu.jsparrow.core.visitor.files.AbstractUseFilesBufferedIOMethodsASTVisitor;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for implementations of
 * {@link AbstractUseFilesBufferedIOMethodsASTVisitor}.
 * 
 * @since 4.8.0
 *
 */
public interface UseFilesBufferedIOMethodsEvent {

	/**
	 * 
	 * @param bufferedIOInstanceCreation
	 *            the initialization of a {@link BufferedReader} or
	 *            {@link BufferedWriter} to be replaced.
	 */
	default void addMarkerEvent(ClassInstanceCreation bufferedIOInstanceCreation) {

	}
}
