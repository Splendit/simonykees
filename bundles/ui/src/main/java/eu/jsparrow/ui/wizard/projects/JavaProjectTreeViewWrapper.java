package eu.jsparrow.ui.wizard.projects;

import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.widgets.Group;

import eu.jsparrow.ui.treeview.AbstractCheckBoxTreeViewWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.AbstractJavaElementWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectsCollector;
import eu.jsparrow.ui.wizard.projects.javaelement.PackageFragmentRootWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.PackageFragmentWrapper;

public class JavaProjectTreeViewWrapper extends AbstractCheckBoxTreeViewWrapper {

	private List<JavaProjectWrapper> javaProjects;
	
	protected JavaProjectTreeViewWrapper(Group group) {
		super(group);
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement == javaProjects) {
			return javaProjects.toArray();
		}
		return new Object[] {};
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		
		if (parentElement instanceof JavaProjectWrapper) {
			return ((JavaProjectWrapper) parentElement).getChildren()
				.toArray();
		}
		if (parentElement instanceof PackageFragmentRootWrapper) {
			return ((PackageFragmentRootWrapper) parentElement).getChildren()
				.toArray();
		}
		if (parentElement instanceof PackageFragmentWrapper) {
			return ((PackageFragmentWrapper) parentElement).getChildren()
				.toArray();
		}
		return new Object[] {};
	}

	@Override
	public Object getParent(Object element) {
		if(element instanceof AbstractJavaElementWrapper) {
			return 	((AbstractJavaElementWrapper) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof AbstractJavaElementWrapper) {
			return 	((AbstractJavaElementWrapper) element).hasChildren();
		}
		return false;
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	protected ILabelProvider createTreeViewerLabelProvider() {
		return new JavaElementLabelProvider();
	}

	@Override
	protected List<JavaProjectWrapper> createInput() {
		if (javaProjects == null) {
			javaProjects = JavaProjectsCollector.collectJavaProjects();
		}
		return javaProjects;
	}

	@Override
	protected void expandTreeNodesSelectively() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateTreeViewerSelectionState() {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		
		
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		
	}

}
