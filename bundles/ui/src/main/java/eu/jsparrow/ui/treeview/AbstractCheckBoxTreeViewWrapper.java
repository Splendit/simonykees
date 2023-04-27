package eu.jsparrow.ui.treeview;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Group;

public abstract class AbstractCheckBoxTreeViewWrapper implements ITreeContentProvider, ICheckStateListener {

	protected CheckboxTreeViewer checkboxTreeViewer;

	protected AbstractCheckBoxTreeViewWrapper(Group group) {
		createCheckBoxTreeViewer(group);
		populateCheckboxTreeViewer();
	}

	protected void createCheckBoxTreeViewer(Group group) {
		checkboxTreeViewer = new CheckboxTreeViewer(group);
		checkboxTreeViewer.getTree()
			.setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxTreeViewer.setContentProvider(this);
		checkboxTreeViewer.setLabelProvider(createTreeViewerLabelProvider());
		checkboxTreeViewer.addCheckStateListener(this);
		checkboxTreeViewer.setComparator(new ViewerComparator());
	}

	public void setTreeViewerFilter(ViewerFilter treeviewerFilter) {
		checkboxTreeViewer.setFilters(treeviewerFilter);
		updateTreeViewerSelectionState();
	}

	public void populateCheckboxTreeViewer() {
		checkboxTreeViewer.setInput(createInput());
		updateTreeViewerSelectionState();
	}

	protected abstract ILabelProvider createTreeViewerLabelProvider();

	protected abstract Object createInput();

	protected abstract void updateTreeViewerSelectionState();
}
