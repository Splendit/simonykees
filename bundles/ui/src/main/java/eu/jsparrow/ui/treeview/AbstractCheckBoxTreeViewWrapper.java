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

public abstract class AbstractCheckBoxTreeViewWrapper
		implements ITreeContentProvider, ICheckStateListener, ITreeViewerListener {

	protected CheckboxTreeViewer checkboxTreeViewer;

	protected void createCheckBoxTreeViewer(Group group, List<? extends ICheckBoxTreeViewNode> elementList) {
		checkboxTreeViewer = new CheckboxTreeViewer(group);
		checkboxTreeViewer.getTree()
			.setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxTreeViewer.setContentProvider(this);
		checkboxTreeViewer.setLabelProvider(createTreeViewerLabelProvider());
		checkboxTreeViewer.addCheckStateListener(this);
		checkboxTreeViewer.setComparator(new ViewerComparator());
		checkboxTreeViewer.addTreeListener(this);
		checkboxTreeViewer.setInput(elementList.toArray());
		updateTreeViewerSelectionState();
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
			ICheckBoxTreeViewNode node = (ICheckBoxTreeViewNode) parentElement;
			return node.getChildrenAsObjectArray();
		}
		return new Object[] {};
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ICheckBoxTreeViewNode) {
			return ((ICheckBoxTreeViewNode) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ICheckBoxTreeViewNode) {
			return ((ICheckBoxTreeViewNode) element).hasChildren();
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

	protected abstract ILabelProvider createTreeViewerLabelProvider();

	protected abstract void updateTreeViewerSelectionState();
}
