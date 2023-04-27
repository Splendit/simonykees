package eu.jsparrow.ui.wizard.projects;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.swt.widgets.Group;

import eu.jsparrow.ui.treeview.AbstractCheckBoxTreeViewWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.AbstractJavaElementWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.AbstractJavaElementWrapperWithChildList;
import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.JavaProjectsCollector;
import eu.jsparrow.ui.wizard.projects.javaelement.PackageFragmentRootWrapper;
import eu.jsparrow.ui.wizard.projects.javaelement.PackageFragmentWrapper;

public class JavaProjectTreeViewWrapper extends AbstractCheckBoxTreeViewWrapper implements ITreeViewerListener {

	private List<JavaProjectWrapper> javaProjects;
	private Set<AbstractJavaElementWrapper> selectedWrappers = new HashSet<>();
	private Set<AbstractJavaElementWrapper> grayedWrappers = new HashSet<>();

	protected JavaProjectTreeViewWrapper(Group group) {
		super(group);
	}

	@Override
	protected void createCheckBoxTreeViewer(Group group) {
		super.createCheckBoxTreeViewer(group);
		checkboxTreeViewer.addTreeListener(this);
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
		if (element instanceof AbstractJavaElementWrapper) {
			return ((AbstractJavaElementWrapper) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof AbstractJavaElementWrapper) {
			return ((AbstractJavaElementWrapper) element).hasChildren();
		}
		return false;
	}

	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object element = event.getElement();
		if (element instanceof AbstractJavaElementWrapper) {
			boolean checked = event.getChecked();
			AbstractJavaElementWrapper wrapper = (AbstractJavaElementWrapper) element;
			checkboxTreeViewer.setChecked(wrapper, checked);
			checkboxTreeViewer.setGrayed(wrapper, false);
			updateSelectedWrappers(wrapper, checked, false);
			updateChildrenSelectionState(wrapper, checked);
			updateParentSelectionState(wrapper);
		}
	}

	protected void updateSelectedWrappers(AbstractJavaElementWrapper wrapper, boolean checked, boolean grayed) {
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

	private void updateChildrenSelectionState(AbstractJavaElementWrapper wrapper, boolean checked) {
		if (wrapper instanceof AbstractJavaElementWrapperWithChildList) {
			AbstractJavaElementWrapperWithChildList<?> wapperWithChildList = (AbstractJavaElementWrapperWithChildList<?>) wrapper;
			if (wapperWithChildList.isChildListAssigned()) {
				for (AbstractJavaElementWrapper child : wapperWithChildList.getChildren()) {
					checkboxTreeViewer.setChecked(child, checked);
					checkboxTreeViewer.setGrayed(child, false);
					updateSelectedWrappers(child, checked, false);
					updateChildrenSelectionState(child, checked);
				}
			}
		}
	}

	private void updateParentSelectionState(AbstractJavaElementWrapper wrapper) {
		AbstractJavaElementWrapper parent = wrapper.getParent();
		if (!(parent instanceof AbstractJavaElementWrapperWithChildList)) {
			return;
		}

		AbstractJavaElementWrapperWithChildList<?> wapperWithChildList = (AbstractJavaElementWrapperWithChildList<?>) parent;

		if (allChildrenChecked(wapperWithChildList)) {
			checkboxTreeViewer.setChecked(wapperWithChildList, true);
			checkboxTreeViewer.setGrayed(wapperWithChildList, false);
			updateSelectedWrappers(wapperWithChildList, true, false);
		} else if (allChildrenUnchecked(wapperWithChildList)) {
			checkboxTreeViewer.setChecked(wapperWithChildList, false);
			checkboxTreeViewer.setGrayed(wapperWithChildList, false);
			updateSelectedWrappers(wapperWithChildList, false, false);
		} else {
			checkboxTreeViewer.setChecked(wapperWithChildList, true);
			checkboxTreeViewer.setGrayed(wapperWithChildList, true);
			updateSelectedWrappers(wapperWithChildList, false, true);
		}
		updateParentSelectionState(wapperWithChildList);
	}

	private boolean allChildrenChecked(AbstractJavaElementWrapperWithChildList<?> wapperWithChildList) {
		for (AbstractJavaElementWrapper child : wapperWithChildList.getChildren()) {
			if (!selectedWrappers.contains(child)) {
				return false;
			}
		}
		return true;
	}

	private boolean allChildrenUnchecked(AbstractJavaElementWrapperWithChildList<?> wapperWithChildList) {
		for (AbstractJavaElementWrapper child : wapperWithChildList.getChildren()) {
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
	protected List<JavaProjectWrapper> createInput() {
		if (javaProjects == null) {
			javaProjects = JavaProjectsCollector.collectJavaProjects();
		}
		return javaProjects;
	}

	@Override
	protected void updateTreeViewerSelectionState() {
		// TODO Auto-generated method stub

	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		// ...
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
			for (AbstractJavaElementWrapper child : wapperWithChildList.getChildren()) {
				checkboxTreeViewer.setChecked(child, checked);
				checkboxTreeViewer.setGrayed(child, false);
				updateSelectedWrappers(child, checked, false);
			}
		}
	}
}
