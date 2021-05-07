package eu.jsparrow.rules.common.markers;

import org.eclipse.jdt.core.IJavaElement;

public interface MarkerEvent {

	public int getOffset();

	public int getLength();

	public String getName();

	public String getMessage();

	public String getDescription();

	public IJavaElement getJavaElement();

}
