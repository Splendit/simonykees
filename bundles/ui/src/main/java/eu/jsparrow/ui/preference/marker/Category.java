package eu.jsparrow.ui.preference.marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class Category {

	private TreeWrapper treeWrapper;
	private List<TreeEntry> categoryEntries = new ArrayList<>();
	private String tag;
	private Button button;
	private TreeItem item;
	private List<String> allActiveMarkers;
	private Composite composite;
	private TreeEditor editor;

	public Category(TreeWrapper treeWrapper, String tag, List<String> allActiveMarkers) {
		this.treeWrapper = treeWrapper;
		this.tag = tag;
		this.allActiveMarkers = allActiveMarkers;
	}

	public void initCategory() {
		Tree tree = treeWrapper.getTree();
		item = new TreeItem(tree, SWT.NONE);
		item.setText(""); //$NON-NLS-1$

		editor = new TreeEditor(tree);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 16;

		composite = new Composite(tree, 0);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		composite.setLayout(gridLayout);

		button = new Button(composite, SWT.CHECK);
		button.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, false, false));

		Label label = new Label(composite, 0);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.DEFAULT, false, false));
		label.setText(StringUtils.capitalize(tag));
		label.setVisible(true);

		editor.setEditor(composite, item);
	}

	public void initCategoryEntries(Map<String, RuleDescription> categoryMarkerDescriptions) {
		List<TreeEntry> entries = new ArrayList<>();
		Comparator<String> comparator = Comparator.comparing(key -> categoryMarkerDescriptions.get(key).getName());
		SortedSet<String> sortedIds = new TreeSet<>(comparator);
		sortedIds.addAll(categoryMarkerDescriptions.keySet());
		for (String id : sortedIds) {
			RuleDescription description = categoryMarkerDescriptions.get(id);
			List<Tag> tags = description.getTags();
			boolean selected = allActiveMarkers.contains(id);
			TreeEntry treeEntry = new TreeEntry(id, selected, description, tags, treeWrapper, item);
			entries.add(treeEntry);
		}
		this.categoryEntries = Collections.unmodifiableList(entries);

		if (categoryEntries.isEmpty()) {
			item.dispose();
			composite.dispose();
			editor.dispose();
		} else {

			// check if every items in a treeCategory -was- selected
			updateCategorySelection();

			// add listeners to remaining treeCategoryButtons
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button source = (Button) e.getSource();

					/*
					 * If click -> loop how many markers exist in treeCategory
					 * Then (de)select all marker's adjacent buttons
					 */
					updateCategoryEntriesSelection(source.getSelection());
				}
			});
		}
	}

	private void updateCategorySelection() {
		if (categoryEntries.isEmpty()) {
			return;
		}
		boolean allChecked = categoryEntries.stream()
			.allMatch(TreeEntry::isSelected);

		if (allChecked) {
			button.setSelection(true);
			button.setGrayed(false);
		} else {
			boolean someChecked = categoryEntries.stream()
				.anyMatch(TreeEntry::isSelected);
			if (someChecked) {
				button.setSelection(true);
				button.setGrayed(true);
			} else {
				button.setSelection(false);
				button.setGrayed(false);
			}
		}

	}

	public void setSelectionByMarker(String markerId, boolean selection) {
		for (TreeEntry treeEntry : categoryEntries) {
			if (markerId.equalsIgnoreCase(treeEntry.getMarkerId())) {
				treeEntry.setSelection(selection);
			}
		}
		updateCategorySelection();
	}

	public void setCategorySelection(boolean selection) {
		if (!categoryEntries.isEmpty()) {
			button.setSelection(selection);
			updateCategoryEntriesSelection(selection);
		}
	}

	public void setEnabledByMarker(String markerId, boolean selection) {
		for (TreeEntry treeEntry : categoryEntries) {
			if (markerId.equalsIgnoreCase(treeEntry.getMarkerId())) {
				if (selection) {
					treeEntry.setEnabled();
				} else {
					treeEntry.setDisabled();
				}
			}
		}
		updateCategorySelection();
	}

	private void updateCategoryEntriesSelection(boolean selection) {
		if (selection) {
			categoryEntries.forEach(TreeEntry::setEnabled);
		} else {
			categoryEntries.forEach(TreeEntry::setDisabled);
		}
	}

	public String getTag() {
		return tag;
	}

	public TreeItem getTreeItem() {
		return item;
	}
}
