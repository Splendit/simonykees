package eu.jsparrow.ui.wizard.projects;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.widgets.Group;

import eu.jsparrow.ui.treeview.AbstractCheckBoxTreeViewWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.AbstractJavaElementWrapperWithChildList;
import eu.jsparrow.ui.wizard.projects.javaelement.IJavaElementWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectWrapper;

public class JavaProjectTreeViewWrapper extends AbstractCheckBoxTreeViewWrapper {

	private Set<IJavaElementWrapper> selectedWrappers = new HashSet<>();
	private Set<IJavaElementWrapper> grayedWrappers = new HashSet<>();
	private List<JavaProjectWrapper> javaProjects;

	protected JavaProjectTreeViewWrapper(Group group, List<JavaProjectWrapper> javaProjects) {
		this.javaProjects = javaProjects;
		this.createCheckBoxTreeViewer(group, javaProjects);
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		if (element instanceof IJavaElementWrapper) {
			boolean checked = event.getChecked();
			IJavaElementWrapper wrapper = (IJavaElementWrapper) element;
			checkboxTreeViewer.setChecked(wrapper, checked);
			checkboxTreeViewer.setGrayed(wrapper, false);
			updateSelectedAndGrayedWrappers(wrapper, checked, false);
			updateChildrenSelectionState(wrapper, checked);
			updateParentSelectionState(wrapper);
		}
	}

	protected void updateSelectedAndGrayedWrappers(IJavaElementWrapper wrapper, boolean checked,
			boolean grayed) {
		if (checked) {
			selectedWrappers.add(wrapper);
			grayedWrappers.remove(wrapper);
		} else if (grayed) {
			selectedWrappers.remove(wrapper);
			grayedWrappers.add(wrapper);
		} else {
			selectedWrappers.remove(wrapper);
			grayedWrappers.remove(wrapper);
		}
	}

	private void updateChildrenSelectionState(IJavaElementWrapper wrapper, boolean checked) {
		if (wrapper instanceof AbstractJavaElementWrapperWithChildList) {
			AbstractJavaElementWrapperWithChildList<?> wapperWithChildList = (AbstractJavaElementWrapperWithChildList<?>) wrapper;
			if (wapperWithChildList.isChildListAssigned()) {
				List<? extends IJavaElementWrapper> children = wapperWithChildList.getChildren();
				for (IJavaElementWrapper child : children) {
					checkboxTreeViewer.setChecked(child, checked);
					checkboxTreeViewer.setGrayed(child, false);
					updateSelectedAndGrayedWrappers(child, checked, false);
					updateChildrenSelectionState(child, checked);
				}
			}
		}
	}

	private void updateParentSelectionState(IJavaElementWrapper wrapper) {
		IJavaElementWrapper parent = wrapper.getParent();
		if (!(parent instanceof AbstractJavaElementWrapperWithChildList)) {
			return;
		}

		AbstractJavaElementWrapperWithChildList<?> wapperWithChildList = (AbstractJavaElementWrapperWithChildList<?>) parent;

		if (allChildrenChecked(wapperWithChildList)) {
			checkboxTreeViewer.setChecked(wapperWithChildList, true);
			checkboxTreeViewer.setGrayed(wapperWithChildList, false);
			updateSelectedAndGrayedWrappers(wapperWithChildList, true, false);
		} else if (allChildrenUnchecked(wapperWithChildList)) {
			checkboxTreeViewer.setChecked(wapperWithChildList, false);
			checkboxTreeViewer.setGrayed(wapperWithChildList, false);
			updateSelectedAndGrayedWrappers(wapperWithChildList, false, false);
		} else {
			checkboxTreeViewer.setChecked(wapperWithChildList, true);
			checkboxTreeViewer.setGrayed(wapperWithChildList, true);
			updateSelectedAndGrayedWrappers(wapperWithChildList, false, true);
		}
		updateParentSelectionState(wapperWithChildList);
	}

	private boolean allChildrenChecked(AbstractJavaElementWrapperWithChildList<?> wapperWithChildList) {
		for (IJavaElementWrapper child : wapperWithChildList.getChildren()) {
			if (!selectedWrappers.contains(child)) {
				return false;
			}
		}
		return true;
	}

	private boolean allChildrenUnchecked(AbstractJavaElementWrapperWithChildList<?> wapperWithChildList) {
		for (IJavaElementWrapper child : wapperWithChildList.getChildren()) {
			if (selectedWrappers.contains(child)) {
				return false;
			}
			if (grayedWrappers.contains(child)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected ILabelProvider createTreeViewerLabelProvider() {
		return new JavaElementLabelProvider();
	}

	@Override
	protected void updateTreeViewerSelectionState() {
		this.javaProjects.forEach(javaProjectWrapper -> setTreeViewerCheckStateForSubTree(javaProjectWrapper, false));
		this.selectedWrappers.forEach(wrapper -> {
			checkboxTreeViewer.setChecked(wrapper, true);
			checkboxTreeViewer.setGrayed(wrapper, false);
		});
		this.grayedWrappers.forEach(wrapper -> {
			checkboxTreeViewer.setChecked(wrapper, true);
			checkboxTreeViewer.setGrayed(wrapper, true);
		});
	}

	private void setTreeViewerCheckStateForSubTree(IJavaElementWrapper javaElementWrapper, boolean checked) {
		setTreeViewerCheckStateForElement(javaElementWrapper, checked);

		if (javaElementWrapper instanceof AbstractJavaElementWrapperWithChildList) {
			AbstractJavaElementWrapperWithChildList<?> wapperWithChildList = (AbstractJavaElementWrapperWithChildList<?>) javaElementWrapper;
			if (wapperWithChildList.isChildListAssigned()) {
				for (IJavaElementWrapper child : wapperWithChildList.getChildren()) {
					setTreeViewerCheckStateForSubTree(child, checked);
				}
			}
		}
	}

	private void setTreeViewerCheckStateForElement(Object element, boolean checked) {
		checkboxTreeViewer.setChecked(element, checked);
		checkboxTreeViewer.setGrayed(element, false);
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		Object element = event.getElement();
		boolean grayed = checkboxTreeViewer.getGrayed(element);
		if (grayed) {
			return;
		}

		if (element instanceof AbstractJavaElementWrapperWithChildList) {
			AbstractJavaElementWrapperWithChildList<?> wapperWithChildList = (AbstractJavaElementWrapperWithChildList<?>) element;
			boolean checked = checkboxTreeViewer.getChecked(element);
			for (IJavaElementWrapper child : wapperWithChildList.getChildren()) {
				checkboxTreeViewer.setChecked(child, checked);
				checkboxTreeViewer.setGrayed(child, false);
				updateSelectedAndGrayedWrappers(child, checked, false);
			}
		}
	}

	public Set<IJavaElementWrapper> getSelectedWrappers() {
		return selectedWrappers;
	}
}
