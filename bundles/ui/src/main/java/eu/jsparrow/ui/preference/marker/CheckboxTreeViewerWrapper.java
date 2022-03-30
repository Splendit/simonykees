package eu.jsparrow.ui.preference.marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Text;

import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;

public class CheckboxTreeViewerWrapper {

	private CheckboxTreeViewer checkboxTreeViewer;
	private List<MarkerItemWrapper> allItems = new ArrayList<>();

	public CheckboxTreeViewerWrapper(CheckboxTreeViewer checkboxTreeViewer) {
		this.checkboxTreeViewer = checkboxTreeViewer;
	}

	public void populateCheckboxTreeView(Map<String, RuleDescription> allMarkerDescriptions,
			List<String> allActiveMarkers) {

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

		udpateMarkerItemSelection(allActiveMarkers);
		updateCategorySelection();

	}

	public void selectMarkers(List<String> allActiveMarkers) {
		bulkUpdate(false);

		allItems.stream()
			.flatMap(item -> item.getChildern()
				.stream())
			.filter(item -> allActiveMarkers.contains(item.getMarkerId()))
			.forEach(item -> this.persistMarkerItemSelection(true, item));

		udpateMarkerItemSelection(allActiveMarkers);
		updateCategorySelection();

	}

	private void udpateMarkerItemSelection(List<String> allActiveMarkers) {
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

	public void createCheckListener(CheckStateChangedEvent event) {
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

	public void createSearchFieldModifyListener(ModifyEvent modifyEvent) {
		Text source = (Text) modifyEvent.getSource();
		String searchText = source.getText();
		if (StringUtils.isEmpty(StringUtils.trim(searchText))) {
			checkboxTreeViewer.setInput(allItems.toArray(new MarkerItemWrapper[] {}));
			udpateMarkerItemSelection(SimonykeesPreferenceManager.getAllActiveMarkers());
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
					.anyMatch(r -> r.getMarkerId()
						.equals(child.getMarkerId()));
				if ((categoryMatch || markerMatch) && !alreadyInResult) {
					searchResult.add(child);
				}
			}
		}
		checkboxTreeViewer.setInput(searchResult.toArray(new MarkerItemWrapper[] {}));
		udpateMarkerItemSelection(SimonykeesPreferenceManager.getAllActiveMarkers());
		updateCategorySelection();

	}

	public void bulkUpdate(boolean selection) {
		allItems.stream()
			.flatMap(item -> item.getChildern()
				.stream())
			.forEach(item -> this.persistMarkerItemSelection(selection, item));
		allItems.forEach(item -> checkboxTreeViewer.setSubtreeChecked(item, selection));
	}
}
