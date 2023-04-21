package eu.jsparrow.ui.treeview;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.ui.treeviewer.generic.GenericTreeContentProvider;
import eu.jsparrow.ui.treeviewer.generic.IContentProviderAdapter;

public abstract class AbstractCheckBoxTreeView {
	protected Text searchField;
	protected CheckboxTreeViewer checkboxTreeViewer;

	protected void createSearchTextField(Group group) {
		Composite searchComposite = new Composite(group, SWT.NONE);
		searchComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		searchComposite.setLayout(new GridLayout(1, true));

		searchField = new Text(searchComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		searchField.setMessage(getSearchFieldMessage());
		GridData searchFieldGridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		searchFieldGridData.widthHint = 180;
		searchField.setLayoutData(searchFieldGridData);
		searchField.addModifyListener(this::textRetrievalModified);
	}

	protected void createCheckBoxTreeViewer(Group group) {
		checkboxTreeViewer = new CheckboxTreeViewer(group);
		checkboxTreeViewer.getTree()
			.setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxTreeViewer.setContentProvider(new GenericTreeContentProvider());
		checkboxTreeViewer.setLabelProvider(createTreeViewerLabelProvider());
		checkboxTreeViewer.setInput("root"); //$NON-NLS-1$
		checkboxTreeViewer.addCheckStateListener(this::checkStateChanged);
		checkboxTreeViewer.setComparator(new ViewerComparator());
		checkboxTreeViewer.addTreeListener(createTreeViewerListener());
	}

	/**
	 * Method for the listener functionality for modifying text in
	 * {@link #searchField}
	 */
	protected void textRetrievalModified(ModifyEvent modifyEvent) {
		Text source = (Text) modifyEvent.getSource();
		String searchText = source.getText();
		checkboxTreeViewer.setFilters(createFilter(searchText));
		updateTreeViewerSelectionState();
	}

	public void updateCheckboxTreeViewerInput() {
		final IContentProviderAdapter[] input = createInput();
		checkboxTreeViewer.setInput(input);
		expandTreeNodesSelectively();
		updateTreeViewerSelectionState();
	}

	protected abstract int getTreeViewerGroupHeight();

	protected abstract String getGroupTitle();

	protected abstract String getSearchFieldMessage();

	protected abstract ILabelProvider createTreeViewerLabelProvider();

	protected abstract ITreeViewerListener createTreeViewerListener();

	/**
	 * Method for the listener functionality to check or uncheck tree nodes in
	 * the {@link CheckboxTreeViewer}.
	 */
	public abstract void checkStateChanged(CheckStateChangedEvent event);

	protected abstract IContentProviderAdapter[] createInput();

	protected abstract void expandTreeNodesSelectively();

	protected abstract void updateTreeViewerSelectionState();

	protected abstract ViewerFilter createFilter(String searchText);
}
