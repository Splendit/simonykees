package eu.jsparrow.ui.wizard.projects.javaelement;

import org.eclipse.jdt.core.IJavaElement;

public interface AbstractJavaElementWrapper  {

	AbstractJavaElementWrapper getParent();

	IJavaElement getJavaElement();
	
	String getElementName();
	
	boolean hasChildren();
}
