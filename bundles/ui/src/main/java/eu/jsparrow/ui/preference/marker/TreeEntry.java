package eu.jsparrow.ui.preference.marker;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
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
	private List<Tag> tags;
	private TreeWrapper treeWrapper;
	private Button button;
	private TreeItem categoryItem;
	private boolean selected;

	public TreeEntry(String markerId, boolean selected, RuleDescription description, List<Tag> tags,
			TreeWrapper treeWrapper, TreeItem categoryItem) {
		this.markerId = markerId;
		this.description = description;
		this.tags = tags;
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
		GridLayout gridLayout = new GridLayout(4, false);
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);

		this.button = new Button(composite, SWT.CHECK);
		button.setText(description.getName());
		button.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, false, false));

		// -- hover UI START
		// the shaft is missing its arrow
		Label shaft = new Label(composite, 0);
		GridData shaftGD = new GridData(SWT.FILL, SWT.DEFAULT, false, false);
		shaftGD.widthHint = 10;
		shaft.setLayoutData(shaftGD);
		shaft.setVisible(false);

		shaft.addPaintListener((PaintEvent e) -> {
			e.gc.drawLine(0, 8, 10, 8);
			e.gc.dispose();
		});

		String allTags = tags.stream()
			.map(Tag::toString)
			.map(String::toLowerCase)
			.sorted(Comparator.reverseOrder())
			.collect(Collectors.joining(", ")); //$NON-NLS-1$
		Label label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.DEFAULT, false, false));
		label.setText(allTags);
		label.setVisible(false);

		editor.setEditor(composite, item);

		button.addListener(SWT.MouseExit, (Event event) -> {
			label.setVisible(false);
			shaft.setVisible(false);
		});

		button.addListener(SWT.MouseEnter, (Event event) -> {
			label.setVisible(true);
			shaft.setVisible(true);
		});
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
