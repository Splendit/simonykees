package eu.jsparrow.ui.preference.marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.core.markers.ResolverVisitorsFactory;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.ui.preference.SimonykeesMarkersPreferencePage;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;

/**
 * Wraps a {@link CheckboxTreeViewer} that is used for de/activating markers in
 * the {@link SimonykeesMarkersPreferencePage}. Provides functionalities for
 * performing bulk updates in the tree and also for searching markers by
 * name/tag.
 * 
 * @since 4.10.0
 *
 */
public class MarkerTreeViewWrapper {

	private Text searchField;
	private CheckboxTreeViewer checkboxTreeViewer;
	private final List<MarkerItemWrapper> allItems = new ArrayList<>();

	public MarkerTreeViewWrapper(Composite mainComposite) {
		Group group = new Group(mainComposite, SWT.NONE);
		group.setText(Messages.SimonykeesMarkersPreferencePage_jSparrowMarkersGroupText);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridData groupLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		groupLayoutData.heightHint = 400;

		Composite searchComposite = new Composite(group, SWT.NONE);
		searchComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		searchComposite.setLayout(new GridLayout(1, true));

		searchField = new Text(searchComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		searchField.setMessage(Messages.SimonykeesMarkersPreferencePage_searchLabelMessage);
		GridData searchFieldGridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		searchFieldGridData.widthHint = 180;
		searchField.setLayoutData(searchFieldGridData);
		searchField.addModifyListener(this::modifyText);


		checkboxTreeViewer = new CheckboxTreeViewer(group);
		checkboxTreeViewer.getTree()
			.setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxTreeViewer.setContentProvider(new MarkerContentProvider());
		checkboxTreeViewer.setLabelProvider(new MarkerLabelProvider());
		checkboxTreeViewer.setInput("root"); //$NON-NLS-1$
		checkboxTreeViewer.addCheckStateListener(this::checkStateChanged);
		checkboxTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Comparator<MarkerItemWrapper> comparator = Comparator
					.comparing(MarkerItemWrapper::getName);
				return comparator.compare((MarkerItemWrapper) e1, (MarkerItemWrapper) e2);
			}
		});
		populateCheckBoxTreeView();
	}

	/**
	 * Creates an entry in the tree for each marker. Markers are grouped in
	 * subtrees by their {@link Tag}s. Markers that contain multiple tags are
	 * shown in the all the corresponding subtrees.
	 * 
	 */
	public void populateCheckBoxTreeView() {

		Map<String, RuleDescription> allMarkerDescriptions = ResolverVisitorsFactory.getAllMarkerDescriptions();
		List<String> tags = Arrays.stream(Tag.getAllTags())
			.filter(StringUtils::isAlphaSpace)
			.filter(tag -> !"free".equalsIgnoreCase(tag)) //$NON-NLS-1$
			.sorted()
			.collect(Collectors.toList());
		for (String tagValue : tags) {
			Tag tag = Tag.getTagForName(tagValue);
			MarkerItemWrapper categoryItem = new MarkerItemWrapper(null, true, tagValue,
					StringUtils.capitalize(tagValue), new ArrayList<>());

			allMarkerDescriptions.forEach((key, value) -> {
				List<Tag> markerTags = value.getTags();
				if (markerTags.contains(tag)) {
					categoryItem.addChild(key, value.getName());
				}
			});
			if (!categoryItem.getChildern()
				.isEmpty()) {
				allItems.add(categoryItem);
			}
		}

		MarkerItemWrapper java16 = new MarkerItemWrapper(null, true, "java 16", "Java 16", new ArrayList<>()); //$NON-NLS-1$ //$NON-NLS-2$

		Map<String, RuleDescription> java11PlusItems = findByJavaVersion(allMarkerDescriptions,
				Arrays.asList(12, 13, 14, 15, 16));
		java11PlusItems.forEach((key, value) -> java16.addChild(key, value.getName()));
		allItems.add(java16);
		MarkerItemWrapper[] input = allItems.toArray(new MarkerItemWrapper[] {});
		checkboxTreeViewer.setInput(input);

		updateMarkerItemSelection();
		updateCategorySelection();

	}

	/**
	 * Activates the given list of markers and deactivates the rest.
	 * 
	 * @param allActiveMarkers
	 *            the marker IDs to be activated.
	 */
	public void selectMarkers(List<String> allActiveMarkers) {
		bulkUpdate(false);

		allItems.stream()
			.flatMap(item -> item.getChildern()
				.stream())
			.filter(item -> allActiveMarkers.contains(item.getMarkerId()))
			.forEach(item -> this.persistMarkerItemSelection(true, item));

		updateMarkerItemSelection();
		updateCategorySelection();

	}

	private void updateMarkerItemSelection() {
		List<String> allActiveMarkers = SimonykeesPreferenceManager.getAllActiveMarkers();
		allItems.stream()
			.flatMap(itemWrapper -> itemWrapper.getChildern()
				.stream())
			.forEach(itemWrapper -> {
				String id = itemWrapper.getMarkerId();
				if (allActiveMarkers.contains(id)) {
					checkboxTreeViewer.setSubtreeChecked(itemWrapper, true);
				}
			});
	}

	private void updateCategorySelection() {
		allItems.stream()
			.forEach(itemWrapper -> {
				List<MarkerItemWrapper> children = itemWrapper.getChildern();
				boolean allChecked = children.stream()
					.allMatch(child -> checkboxTreeViewer.getChecked(child));
				boolean noneChecked = children.stream()
					.noneMatch(child -> checkboxTreeViewer.getChecked(child));
				if (allChecked) {
					checkboxTreeViewer.setChecked(itemWrapper, true);
					checkboxTreeViewer.setGrayed(itemWrapper, false);
				} else if (noneChecked) {
					checkboxTreeViewer.setChecked(itemWrapper, false);
					checkboxTreeViewer.setGrayed(itemWrapper, false);
				} else {
					checkboxTreeViewer.setChecked(itemWrapper, true);
					checkboxTreeViewer.setGrayed(itemWrapper, true);
				}
			});
	}

	private Map<String, RuleDescription> findByJavaVersion(Map<String, RuleDescription> allMarkerDescriptions,
			List<Integer> versions) {
		Map<String, RuleDescription> map = new HashMap<>();
		for (Map.Entry<String, RuleDescription> entry : allMarkerDescriptions.entrySet()) {
			RuleDescription description = entry.getValue();
			for (Tag ruleTag : description.getTags()) {
				boolean matched = ruleTag.getTagNames()
					.stream()
					.filter(StringUtils::isNumeric)
					.map(Integer::parseInt)
					.anyMatch(versions::contains);
				if (matched) {
					map.put(entry.getKey(), description);
				}
			}
		}
		return map;
	}

	/**
	 * Method for a listener to check and uncheck markers in the
	 * {@link CheckboxTreeViewer}. Markers with the same ID occurring in
	 * multiple subtrees are simultaneously checked/unchecked.
	 * 
	 * @param event
	 *            the generated event.
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		MarkerItemWrapper treeEntryWrapper = (MarkerItemWrapper) event.getElement();
		boolean checked = event.getChecked();
		checkboxTreeViewer.setSubtreeChecked(treeEntryWrapper, checked);
		Set<String> updated = new HashSet<>();

		if (treeEntryWrapper.isParent()) {
			List<MarkerItemWrapper> children = treeEntryWrapper.getChildern();
			for (MarkerItemWrapper item : children) {
				// Update the preference store for this item.
				updated.add(item.getMarkerId());
				persistMarkerItemSelection(checked, item);
			}
		} else {
			updated.add(treeEntryWrapper.getMarkerId());
			persistMarkerItemSelection(checked, treeEntryWrapper);
		}

		allItems.stream()
			.flatMap(item -> item.getChildern()
				.stream())
			.filter(item -> updated.contains(item.getMarkerId()))
			.forEach(item -> checkboxTreeViewer.setChecked(item, checked));

		updateCategorySelection();
	}

	private void persistMarkerItemSelection(boolean checked, MarkerItemWrapper item) {
		if (checked) {
			SimonykeesPreferenceManager.addActiveMarker(item.getMarkerId());
		} else {
			SimonykeesPreferenceManager.removeActiveMarker(item.getMarkerId());
		}
	}

	/**
	 * Method for the listener functionality for modifying the search field in
	 * {@link SimonykeesMarkersPreferencePage}. As soon as the search field is
	 * modified, the tree view is converted into a flat view.
	 * 
	 * @param modifyEvent
	 *            the generated event.
	 */
	public void modifyText(ModifyEvent modifyEvent) {
		Text source = (Text) modifyEvent.getSource();
		String searchText = source.getText();
		if (StringUtils.isEmpty(StringUtils.trim(searchText))) {
			checkboxTreeViewer.setInput(allItems.toArray(new MarkerItemWrapper[] {}));
			updateMarkerItemSelection();
			updateCategorySelection();
			return;
		}
		Set<MarkerItemWrapper> searchResult = new HashSet<>();
		for (MarkerItemWrapper item : allItems) {
			boolean categoryMatch = StringUtils.contains(StringUtils.lowerCase(item.getName()),
					StringUtils.lowerCase(searchText));
			List<MarkerItemWrapper> children = item.getChildern();
			for (MarkerItemWrapper child : children) {
				boolean markerMatch = StringUtils.contains(
						StringUtils.lowerCase(child.getName()),
						StringUtils.lowerCase(searchText));
				boolean alreadyInResult = searchResult.stream()
					.map(MarkerItemWrapper::getMarkerId)
					.anyMatch(r -> r.equals(child.getMarkerId()));
				if ((categoryMatch || markerMatch) && !alreadyInResult) {
					searchResult.add(child);
				}
			}
		}
		checkboxTreeViewer.setInput(searchResult.toArray(new MarkerItemWrapper[] {}));
		updateMarkerItemSelection();
		updateCategorySelection();

	}

	/**
	 * Activates or deactivates all the entries of the
	 * {@link CheckboxTreeViewer}. The preference store is updated accordingly.
	 * 
	 * @param selection
	 *            whether the markers should be activated or deactivated.
	 */
	public void bulkUpdate(boolean selection) {
		allItems.stream()
			.flatMap(item -> item.getChildern()
				.stream())
			.forEach(item -> this.persistMarkerItemSelection(selection, item));
		allItems.forEach(item -> checkboxTreeViewer.setSubtreeChecked(item, selection));
	}

	public void setSearchFieldText(String string) {
		searchField.setText(string);
	}
}
