package eu.jsparrow.ui.wizard.projects.javaelement;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

import eu.jsparrow.ui.treeview.ICheckBoxTreeViewNode;

public interface IJavaElementWrapper extends ICheckBoxTreeViewNode<IJavaElementWrapper> {

	@Override
	IJavaElementWrapper getParent();

	IJavaElement getJavaElement();

	String getElementName();

	@Override
	List<IJavaElementWrapper> getChildren();

	@Override
	boolean hasChildren();
}
