package eu.jsparrow.ui.preference.marker;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;

/**
 * Represents a leaf in the jSparrow Markers Preference page tree-view.
 * 
 * @since 4.7.0
 *
 */
public class TreeEntry {

	private String markerId;
	private RuleDescription description;
	private TreeWrapper treeWrapper;
	private Button button;
	private TreeItem categoryItem;
	private boolean selected;

	public TreeEntry(String markerId, boolean selected, RuleDescription description,
			TreeWrapper treeWrapper, TreeItem categoryItem) {
		this.markerId = markerId;
		this.description = description;
		this.treeWrapper = treeWrapper;
		this.categoryItem = categoryItem;
		this.selected = selected;
		initTreeEntry();

	}

	public boolean isSelected() {
		return button.getSelection();
	}

	public void setSelection(boolean selection) {
		button.setSelection(selection);
	}

	public String getMarkerId() {
		return this.markerId;
	}

	private void initTreeEntry() {
		TreeItem item = new TreeItem(categoryItem, SWT.NONE);

		// add UI through editor
		Tree tree = treeWrapper.getTree();
		TreeEditor editor = new TreeEditor(tree);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 16;

		Composite composite = new Composite(tree, 0);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);

		this.button = new Button(composite, SWT.CHECK);
		button.setText(description.getName());
		button.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, false, false));

		editor.setEditor(composite, item);
		// -- hover UI END

		button.setSelection(selected);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = (Button) e.getSource();
				if (source.getSelection()) {
					enable();
				} else {
					disable();
				}
			}
		});
	}

	/**
	 * Modifies the preference store and updates checks button for this jSparrow
	 * Marker.
	 */
	public void setEnabled() {
		setSelection(true);
		enable();
	}

	/**
	 * Modifies the preference store and updates unchecks button for this
	 * jSparrow Marker.
	 */
	public void setDisabled() {
		setSelection(false);
		disable();
	}

	private void disable() {
		SimonykeesPreferenceManager.removeActiveMarker(markerId);
		treeWrapper.setSelectionByMarkerId(markerId, false);
	}

	private void enable() {
		SimonykeesPreferenceManager.addActiveMarker(markerId);
		treeWrapper.setSelectionByMarkerId(markerId, true);
	}

}
