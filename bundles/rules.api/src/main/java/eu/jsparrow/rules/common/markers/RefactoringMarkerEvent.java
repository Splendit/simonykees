package eu.jsparrow.rules.common.markers;

import org.eclipse.jdt.core.IJavaElement;

public interface RefactoringMarkerEvent {

	String getResolver();

	int getOffset();

	int getLength();
	
	int getHighlightLength();

	String getName();

	String getMessage();

	String getDescription();

	IJavaElement getJavaElement();

}
