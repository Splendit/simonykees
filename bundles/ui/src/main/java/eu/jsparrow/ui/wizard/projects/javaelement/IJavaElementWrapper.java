package eu.jsparrow.ui.wizard.projects.javaelement;

import org.eclipse.jdt.core.IJavaElement;

import eu.jsparrow.ui.treeview.ICheckBoxTreeViewNode;

public interface IJavaElementWrapper extends ICheckBoxTreeViewNode {

	@Override
	IJavaElementWrapper getParent();

	IJavaElement getJavaElement();

	String getElementName();

	@Override
	boolean hasChildren();
}
