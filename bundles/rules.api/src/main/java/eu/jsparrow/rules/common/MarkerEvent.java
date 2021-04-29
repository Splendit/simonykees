package eu.jsparrow.rules.common;

import org.eclipse.jdt.core.IJavaElement;

public interface MarkerEvent {
	
	public int getIndex();

	public int getOffset();

	public int getLength();

	public String getMessage();
	
	public IJavaElement getJavaElement();

}
