package eu.jsparrow.ui.treeview;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractCheckBoxTreeView implements ITreeContentProvider, ICheckStateListener {
	protected Text searchField;
	protected CheckboxTreeViewer checkboxTreeViewer;


	protected void createCheckBoxTreeViewer(Group group) {
		checkboxTreeViewer = new CheckboxTreeViewer(group);
		checkboxTreeViewer.getTree()
			.setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxTreeViewer.setContentProvider(this);
		checkboxTreeViewer.setLabelProvider(createTreeViewerLabelProvider());
		checkboxTreeViewer.setInput("root"); //$NON-NLS-1$
		checkboxTreeViewer.addCheckStateListener(this);
		checkboxTreeViewer.setComparator(new ViewerComparator());
		checkboxTreeViewer.addTreeListener(createTreeViewerListener());
	}


	protected void setTreeViewerFilter(ViewerFilter treeviewerFilter) {
		checkboxTreeViewer.setFilters(treeviewerFilter);
		updateTreeViewerSelectionState();
	}

	public void populateCheckboxTreeViewer() {
		checkboxTreeViewer.setInput(createInput());
		expandTreeNodesSelectively();
		updateTreeViewerSelectionState();
	}

	protected abstract int getTreeViewerGroupHeight();

	protected abstract ILabelProvider createTreeViewerLabelProvider();

	protected abstract ITreeViewerListener createTreeViewerListener();

	protected abstract Object createInput();

	protected abstract void expandTreeNodesSelectively();

	protected abstract void updateTreeViewerSelectionState();
}
