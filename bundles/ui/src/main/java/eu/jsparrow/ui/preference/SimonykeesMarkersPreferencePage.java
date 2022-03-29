package eu.jsparrow.ui.preference;

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
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.jsparrow.core.markers.ResolverVisitorsFactory;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.ui.preference.marker.MarkerContentProvider;
import eu.jsparrow.ui.preference.marker.MarkerItemWrapper;
import eu.jsparrow.ui.preference.marker.MarkerLabelProvider;
import eu.jsparrow.ui.preference.marker.TreeWrapper;
import eu.jsparrow.ui.preference.profile.DefaultActiveMarkers;

/**
 * A preference page for activating and deactivating jSparrow Markers.
 * 
 * @since 4.6.0
 *
 */
public class SimonykeesMarkersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private TreeWrapper treeWrapper;

	private CheckboxTreeViewer checkboxTreeViewer;
	private Text searchField;

	@Override
	public void init(IWorkbench workbench) {
		// required by the parent
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setFont(parent.getFont());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		mainComposite.setLayoutData(gd);
		mainComposite.setLayout(new GridLayout(1, true));

		Composite searchComposite = new Composite(mainComposite, SWT.NONE);
		searchComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		searchComposite.setLayout(new GridLayout(1, true));

		searchField = new Text(searchComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		searchField.setMessage("Search");
		GridData searchFieldGridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		searchFieldGridData.widthHint = 180;
		searchField.setLayoutData(searchFieldGridData);

		Group group = new Group(mainComposite, SWT.NONE);
		group.setText(Messages.SimonykeesMarkersPreferencePage_jSparrowMarkersGroupText);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridData groupLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		groupLayoutData.heightHint = 400;

		checkboxTreeViewer = new CheckboxTreeViewer(group);
		checkboxTreeViewer.getTree()
			.setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxTreeViewer.setContentProvider(new MarkerContentProvider());
		checkboxTreeViewer.setLabelProvider(new MarkerLabelProvider());
		checkboxTreeViewer.setInput("root"); //$NON-NLS-1$

		Map<String, RuleDescription> allMarkerDescriptions = ResolverVisitorsFactory.getAllMarkerDescriptions();
		List<String> allActiveMarkers = SimonykeesPreferenceManager.getAllActiveMarkers();
		List<MarkerItemWrapper> allItems = populateCheckboxTreeView(allMarkerDescriptions, allActiveMarkers);
		checkboxTreeViewer.addCheckStateListener(event -> this.createCheckListener(event, allItems));
		checkboxTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Comparator<MarkerItemWrapper> comparator = Comparator
					.comparing(MarkerItemWrapper::getName);
				return comparator.compare((MarkerItemWrapper) e1, (MarkerItemWrapper) e2);
			}
		});

		searchField.addModifyListener((ModifyEvent e) -> {
			Text source = (Text) e.getSource();
			String searchText = source.getText();
			if (StringUtils.isEmpty(StringUtils.trim(searchText))) {
				checkboxTreeViewer.setInput(allItems.toArray(new MarkerItemWrapper[] {}));
				return;
			}
			Set<MarkerItemWrapper> searchResult = new HashSet<>();
			for (MarkerItemWrapper item : allItems) {
				boolean categoryMatch = StringUtils.contains(StringUtils.lowerCase(item.getName()),
						StringUtils.lowerCase(searchText));

				List<MarkerItemWrapper> children = item.getChildern();
				for (MarkerItemWrapper child : children) {
					boolean markerMatch = StringUtils.contains(StringUtils.lowerCase(child.getName()),
							StringUtils.lowerCase(searchText));
					if (categoryMatch || markerMatch) {
						searchResult.add(child);
					}
				}

			}

			checkboxTreeViewer.setInput(searchResult.toArray(new MarkerItemWrapper[] {}));
		});

		Composite bulkActionsComposite = new Composite(mainComposite, SWT.NONE);
		bulkActionsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		bulkActionsComposite.setLayout(new GridLayout(2, false));
		addButton(bulkActionsComposite, Messages.SimonykeesMarkersPreferencePage_enableAll, true, treeWrapper);
		addButton(bulkActionsComposite, Messages.SimonykeesMarkersPreferencePage_disableAll, false, treeWrapper);

		return mainComposite;
	}

	private List<MarkerItemWrapper> populateCheckboxTreeView(Map<String, RuleDescription> allMarkerDescriptions,
			List<String> allActiveMarkers) {
		List<MarkerItemWrapper> allItems = new ArrayList<>();
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
		allItems.add(java16);
		Map<String, RuleDescription> java11PlusItems = findByJavaVersion(allMarkerDescriptions,
				Arrays.asList(12, 13, 14, 15, 16));
		java11PlusItems.forEach((key, value) -> java16.addChild(key, value.getName()));
		MarkerItemWrapper[] input = allItems.toArray(new MarkerItemWrapper[] {});
		checkboxTreeViewer.setInput(input);

		allItems.stream()
			.flatMap(itemWrapper -> itemWrapper.getChildern()
				.stream())
			.forEach(itemWrapper -> {
				String id = itemWrapper.getMarkerId();
				if (allActiveMarkers.contains(id)) {
					checkboxTreeViewer.setSubtreeChecked(itemWrapper, true);
				}
			});
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

		return allItems;

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

	protected void addActiveMarker(String markerId) {
		SimonykeesPreferenceManager.addActiveMarker(markerId);
	}

	protected void removeActiveMarker(String markerId) {
		SimonykeesPreferenceManager.removeActiveMarker(markerId);
	}

	protected void addButton(Composite composite, String name, boolean turn, TreeWrapper treeWrapper) {
		Button thisButton = new Button(composite, SWT.PUSH);
		thisButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		thisButton.setText(name);
		thisButton.addListener(SWT.MouseDown, event -> treeWrapper.bulkUpdateAllCategories(turn));
	}

	private void createCheckListener(CheckStateChangedEvent event, List<MarkerItemWrapper> allItems) {
		MarkerItemWrapper wrapper = (MarkerItemWrapper) event.getElement();
		boolean checked = event.getChecked();
		checkboxTreeViewer.setSubtreeChecked(wrapper, checked);
		Set<String> updated = new HashSet<>();

		if (wrapper.isParent()) {
			List<MarkerItemWrapper> children = wrapper.getChildern();
			for (MarkerItemWrapper item : children) {
				// Update the preference store for this item.
				updated.add(item.getMarkerId());
				if (checked) {
					SimonykeesPreferenceManager.addActiveMarker(item.getMarkerId());
				} else {
					SimonykeesPreferenceManager.removeActiveMarker(item.getMarkerId());
				}
			}

		} else {
			updated.add(wrapper.getMarkerId());
			if (checked) {
				SimonykeesPreferenceManager.addActiveMarker(wrapper.getMarkerId());
			} else {
				SimonykeesPreferenceManager.removeActiveMarker(wrapper.getMarkerId());
			}
		}

		allItems.stream()
			.flatMap(item -> item.getChildern()
				.stream())
			.filter(item -> updated.contains(item.getMarkerId()))
			.forEach(item -> checkboxTreeViewer.setChecked(item, checked));

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

	@Override
	protected void performDefaults() {
		super.performDefaults();
		if (treeWrapper != null) {
			treeWrapper.bulkUpdateAllCategories(false);
			DefaultActiveMarkers defaultMarkers = new DefaultActiveMarkers();
			for (String marker : defaultMarkers.getActiveMarkers()) {
				treeWrapper.setEnabledByMarkerId(marker, true);
			}
		}
	}



	

	
}
