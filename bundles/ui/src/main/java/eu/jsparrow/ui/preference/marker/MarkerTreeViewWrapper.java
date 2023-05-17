package eu.jsparrow.ui.preference.marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.swt.widgets.Group;

import eu.jsparrow.core.markers.ResolverVisitorsFactory;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.ui.preference.SimonykeesMarkersPreferencePage;
import eu.jsparrow.ui.preference.SimonykeesPreferenceManager;
import eu.jsparrow.ui.preference.profile.DefaultActiveMarkers;
import eu.jsparrow.ui.treeview.AbstractCheckBoxTreeViewWrapper;

/**
 * Wraps a {@link CheckboxTreeViewer} that is used for de/activating markers in
 * the {@link SimonykeesMarkersPreferencePage}. Provides functionalities for
 * performing bulk updates in the tree and also for searching markers by
 * name/tag.
 * 
 * @since 4.10.0
 *
 */
public class MarkerTreeViewWrapper extends AbstractCheckBoxTreeViewWrapper<MarkerItemWrapper> {

	public static List<MarkerItemWrapper> createMarkerItemWrapperList() {
		List<MarkerItemWrapper> allItems = new ArrayList<>();
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
			if (!categoryItem.getChildren()
				.isEmpty()) {
				allItems.add(categoryItem);
			}
		}

		MarkerItemWrapper java16 = new MarkerItemWrapper(null, true, "java 16", "Java 16", new ArrayList<>()); //$NON-NLS-1$ //$NON-NLS-2$

		Map<String, RuleDescription> java11PlusItems = findByJavaVersion(allMarkerDescriptions,
				Arrays.asList(12, 13, 14, 15, 16));
		java11PlusItems.forEach((key, value) -> java16.addChild(key, value.getName()));
		allItems.add(java16);
		return allItems;
	}

	private static Map<String, RuleDescription> findByJavaVersion(Map<String, RuleDescription> allMarkerDescriptions,
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

	public MarkerTreeViewWrapper(Group group) {
		this.createCheckBoxTreeViewer(group, createMarkerItemWrapperList());
		List<String> allActiveMarkers = SimonykeesPreferenceManager.getAllActiveMarkers();
		storeMarkersSelectionState(true, allActiveMarkers);
		updateTreeViewerSelectionState();
	}

	private void storeMarkersSelectionState(boolean checked, Collection<String> updated) {
		elements.stream()
			.flatMap(item -> item.getChildren()
				.stream())
			.filter(item -> updated.contains(item.getMarkerId()))
			.forEach(item -> selectionStateStore.setSelectionState(item, checked));
	}

	public List<MarkerItemWrapper> getAllMarkerItemWrappers() {
		return elements;
	}

	/**
	 * Activates the given list of markers and deactivates the rest.
	 * 
	 * @param allActiveMarkers
	 *            the marker IDs to be activated.
	 */
	public void selectDefaultMarkers(DefaultActiveMarkers defaultMarkers) {
		selectionStateStore.unselectAll();
		List<String> allActiveMarkers = defaultMarkers.getActiveMarkers();
		storeMarkersSelectionState(true, allActiveMarkers);
		updateTreeViewerSelectionState();
	}

	/**
	 * Activates or deactivates all the entries of the
	 * {@link CheckboxTreeViewer}. The preference store is updated accordingly.
	 * 
	 * @param selection
	 *            whether the markers should be activated or deactivated.
	 */
	public void bulkUpdate(boolean selection) {
		if (selection) {
			selectionStateStore.setSelectionState(elements, true);
		} else {
			selectionStateStore.unselectAll();
		}
		updateTreeViewerSelectionState();
	}

	/**
	 * 
	 * {@inheritDoc}
	 * 
	 * 
	 * Method for the listener functionality to check or uncheck markers in the
	 * {@link CheckboxTreeViewer}. Markers with the same ID occurring in
	 * multiple subtrees are simultaneously checked/unchecked.
	 */
	@Override
	public void checkStateChanged(CheckStateChangedEvent event) {

		MarkerItemWrapper treeEntryWrapper = (MarkerItemWrapper) event.getElement();
		boolean checked = event.getChecked();
		Set<String> updated = new HashSet<>();

		if (treeEntryWrapper.isParent()) {
			List<MarkerItemWrapper> children = treeEntryWrapper.getChildren();
			for (MarkerItemWrapper item : children) {
				updated.add(item.getMarkerId());
			}
		} else {
			updated.add(treeEntryWrapper.getMarkerId());
		}

		storeMarkersSelectionState(checked, updated);
		updateTreeViewerSelectionState();
	}

	public Set<String> getSelectedMarkersToApply() {
		return selectionStateStore.getSelectedElements()
			.stream()
			.filter(element -> !element.isParent())
			.map(MarkerItemWrapper::getMarkerId)
			.collect(Collectors.toSet());
	}
}
