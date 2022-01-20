package eu.jsparrow.rules.common.markers;

import org.eclipse.jdt.core.IJavaElement;

/**
 * A type providing all the required information for creating a jSparrow marker.
 * 
 * @since 4.0.0
 *
 */
public interface RefactoringMarkerEvent {

	/**
	 * 
	 * @return the id (i.e., the fully qualified name) of the resolver of this
	 *         marker.
	 */
	String getResolver();

	/**
	 * 
	 * @return the offset in the compilation unit for this marker.
	 */
	int getOffset();

	/**
	 * 
	 * @return the length of the node responsible for creating this marker.
	 */
	int getLength();

	/**
	 * 
	 * @return the length of the code to highlight after the marker is resolved
	 *         by the specified resolver.
	 */
	int getHighlightLength();

	/**
	 * 
	 * @return the short summary description of this marker.
	 */
	String getName();

	/**
	 * 
	 * @return the user message shown for this marker.
	 */
	String getMessage();

	/**
	 * 
	 * @return the code preview of this marker.
	 */
	String getCodePreview();

	/**
	 * 
	 * @return the {@link IJavaElement} representing the resource where the marker shall attached to. 
	 */
	IJavaElement getJavaElement();

	/**
	 *  
	 * @return the credit to consider in pay-per-use license model
	 */
	int getWeightValue();

	/**
	 * 
	 * @return the location in the compilation unit where the event occurred. 
	 */
	int getLineNumber();
}
