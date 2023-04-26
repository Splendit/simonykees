package eu.jsparrow.ui.wizard.projects.javaelement;

import org.eclipse.jdt.core.IJavaElement;

public abstract class AbstractJavaElementWrapper {

	public abstract AbstractJavaElementWrapper getParent();

	public abstract IJavaElement getJavaElement();

	public abstract String getElementName();

	public abstract boolean hasChildren();
}
