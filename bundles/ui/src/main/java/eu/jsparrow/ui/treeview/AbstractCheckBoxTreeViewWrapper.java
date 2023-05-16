package eu.jsparrow.ui.treeview;

import java.util.List;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;

public abstract class AbstractCheckBoxTreeViewWrapper<T extends ICheckBoxTreeViewNode<T>>
		implements ITreeContentProvider, ICheckStateListener, ITreeViewerListener {

	protected List<T> elements;
	protected CheckBoxSelectionStateStore<T> selectionStateStore = new CheckBoxSelectionStateStore<>();
	protected CheckboxTreeViewer checkboxTreeViewer;

	protected void createCheckBoxTreeViewer(Group group, List<T> elementList) {
		this.elements = elementList;
		checkboxTreeViewer = new CheckboxTreeViewer(group);
		checkboxTreeViewer.getTree()
			.setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxTreeViewer.setContentProvider(this);
		checkboxTreeViewer.setLabelProvider(createTreeViewerLabelProvider());
		checkboxTreeViewer.addCheckStateListener(this);
		checkboxTreeViewer.setComparator(new ViewerComparator());
		checkboxTreeViewer.addTreeListener(this);
		checkboxTreeViewer.setInput(elements.toArray());
	}

	public void setTreeViewerFilters(ViewerFilter... filters) {
		checkboxTreeViewer.setFilters(filters);
		updateTreeViewerSelectionState();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return (Object[]) inputElement;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ICheckBoxTreeViewNode) {
			ICheckBoxTreeViewNode<?> node = (ICheckBoxTreeViewNode<?>) parentElement;
			return node.getChildren()
				.toArray();
		}
		return new Object[] {};
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ICheckBoxTreeViewNode) {
			return ((ICheckBoxTreeViewNode<?>) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ICheckBoxTreeViewNode) {
			return ((ICheckBoxTreeViewNode<?>) element).hasChildren();
		}
		return false;
	}

	@Override
	public void treeCollapsed(TreeExpansionEvent event) {
		// doing nothing and can be overridden optionally
	}

	@Override
	public void treeExpanded(TreeExpansionEvent event) {
		// doing nothing and can be overridden optionally
	}

	protected void setTreeViewerUnselectedForSubTree(T element) {
		setTreeViewerUnselectedForElement(element);
		if (element.hasChildListAtHand()) {
			for (T child : element.getChildren()) {
				setTreeViewerUnselectedForSubTree(child);
			}
		}
	}

	protected void setTreeViewerUnselectedForElement(T element) {
		checkboxTreeViewer.setChecked(element, false);
		checkboxTreeViewer.setGrayed(element, false);
	}

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

	protected abstract ILabelProvider createTreeViewerLabelProvider();
}
