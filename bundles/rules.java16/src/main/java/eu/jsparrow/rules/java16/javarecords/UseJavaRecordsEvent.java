package eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * An interface to add {@link RefactoringMarkerEvent}s for
 * {@link UseJavaRecordsASTVisitor}.
 * 
 * @since 4.7.0
 */
public interface UseJavaRecordsEvent {

	/**
	 * Creates an instance of {@link RefactoringEventImplt} and records it as a
	 * {@link RefactoringMarkerEvent}. The default implementation is empty.
	 * 
	 * @param original
	 *            the original type declaration to be replaced with a record.
	 *            Uses the rule description in the marker preview.
	 */
	default void addMarkerEvent(TypeDeclaration original) {
	}
}
