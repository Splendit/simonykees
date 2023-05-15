package eu.jsparrow.ui.wizard.projects;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.widgets.Group;

import eu.jsparrow.ui.treeview.AbstractCheckBoxTreeViewWrapper;
import eu.jsparrow.ui.treeview.CheckBoxSelectionStateStore;
import eu.jsparrow.ui.wizard.projects.javaelement.AbstractJavaElementWrapperWithChildList;
import eu.jsparrow.ui.wizard.projects.javaelement.IJavaElementWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectWrapper;

public class JavaProjectTreeViewWrapper extends AbstractCheckBoxTreeViewWrapper<IJavaElementWrapper> {

	private CheckBoxSelectionStateStore<IJavaElementWrapper> selectionStateStore = new CheckBoxSelectionStateStore<>();

	private static List<IJavaElementWrapper> projectsToElementList(List<JavaProjectWrapper> javaProjects) {
		 List<IJavaElementWrapper> elements =  new ArrayList<>();
		 javaProjects.forEach(elements::add);
		 return elements;
	}

	protected JavaProjectTreeViewWrapper(Group group, List<JavaProjectWrapper> javaProjects) {
		this.createCheckBoxTreeViewer(group, projectsToElementList(javaProjects));
	}
	
	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		if (element instanceof IJavaElementWrapper) {
			boolean checked = event.getChecked();
			selectionStateStore.setSelectionState((IJavaElementWrapper) element, checked);
			updateTreeViewerSelectionState();
		}
	}

	@Override
	protected ILabelProvider createTreeViewerLabelProvider() {
		return new JavaElementLabelProvider();
	}

	@Override
	protected void updateTreeViewerSelectionState() {
		this.elements.forEach(this::setTreeViewerUnselectedForSubTree);

		selectionStateStore.getSelectedElements()
			.forEach(wrapper -> {
				checkboxTreeViewer.setChecked(wrapper, true);
				checkboxTreeViewer.setGrayed(wrapper, false);
			});

		selectionStateStore.getGrayedElements()
			.forEach(wrapper -> {
				checkboxTreeViewer.setChecked(wrapper, true);
				checkboxTreeViewer.setGrayed(wrapper, true);
			});
	}

	private void setTreeViewerUnselectedForSubTree(IJavaElementWrapper javaElementWrapper) {
		setTreeViewerUnselectedForElement(javaElementWrapper);

		if (javaElementWrapper instanceof AbstractJavaElementWrapperWithChildList) {
			AbstractJavaElementWrapperWithChildList wapperWithChildList = (AbstractJavaElementWrapperWithChildList) javaElementWrapper;
			if (wapperWithChildList.hasChildListAtHand()) {
				for (IJavaElementWrapper child : wapperWithChildList.getChildren()) {
					setTreeViewerUnselectedForSubTree(child);
				}
			}
		}
	}

	private void setTreeViewerUnselectedForElement(IJavaElementWrapper element) {
		checkboxTreeViewer.setChecked(element, false);
		checkboxTreeViewer.setGrayed(element, false);
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		Object element = event.getElement();
		boolean grayed = checkboxTreeViewer.getGrayed(element);
		if (!grayed && element instanceof AbstractJavaElementWrapperWithChildList) {
			boolean checked = checkboxTreeViewer.getChecked(element);
			selectionStateStore.setSelectionState((AbstractJavaElementWrapperWithChildList) element, checked);
			updateTreeViewerSelectionState();
		}
	}

	public Set<IJavaElementWrapper> getSelectedWrappers() {
		return selectionStateStore.getSelectedElements();
	}
}
