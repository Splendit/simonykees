package eu.jsparrow.ui.treeview;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractCheckBoxTreeView {
	private String searchText;
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
		checkboxTreeViewer.setContentProvider(createTreeViewerContentProvider());
		checkboxTreeViewer.setLabelProvider(createTreeViewerLabelProvider());
		checkboxTreeViewer.setInput("root"); //$NON-NLS-1$
		checkboxTreeViewer.addCheckStateListener(this::checkStateChanged);
		checkboxTreeViewer.setComparator(createTreeViewerComparator());
		checkboxTreeViewer.addTreeListener(createTreeViewerListener());
	}

	/**
	 * Method for the listener functionality for modifying text in
	 * {@link #searchField}
	 */
	protected void textRetrievalModified(ModifyEvent modifyEvent) {
		Text source = (Text) modifyEvent.getSource();
		searchText = source.getText();
		updateCheckboxTreeViewerInput();
	}

	public void updateCheckboxTreeViewerInput() {
		final Object input = createInput();
		checkboxTreeViewer.setInput(input);
		expandTreeNodesSelectively();
		updateTreeViewerSelectionState();
	}

	protected Object createInput() {
		if (searchText == null || StringUtils.isEmpty(StringUtils.trim(searchText))) {
			return createAllAvailableInput();
		}
		return createFilteredInput(searchText);
	}

	protected abstract int getTreeViewerGroupHeight();

	protected abstract String getGroupTitle();

	protected abstract String getSearchFieldMessage();

	protected abstract IContentProvider createTreeViewerContentProvider();

	protected abstract IBaseLabelProvider createTreeViewerLabelProvider();

	protected abstract ViewerComparator createTreeViewerComparator();

	protected abstract ITreeViewerListener createTreeViewerListener();

	/**
	 * Method for the listener functionality to check or uncheck tree nodes in
	 * the {@link CheckboxTreeViewer}.
	 */
	public abstract void checkStateChanged(CheckStateChangedEvent event);

	protected abstract Object createAllAvailableInput();

	/**
	 * 
	 * @param textRetrievalFilter
	 *            expected to be not null and not empty
	 */
	protected abstract Object createFilteredInput(String textRetrievalFilter);

	protected abstract void expandTreeNodesSelectively();

	protected abstract void updateTreeViewerSelectionState();
}
