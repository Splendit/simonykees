package eu.jsparrow.ui.preference;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
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
import eu.jsparrow.ui.preference.marker.CheckboxTreeViewerWrapper;
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
	private CheckboxTreeViewerWrapper treeViewerWrapper;

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

		Group group = new Group(mainComposite, SWT.NONE);
		group.setText(Messages.SimonykeesMarkersPreferencePage_jSparrowMarkersGroupText);
		group.setLayout(new GridLayout(1, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridData groupLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		groupLayoutData.heightHint = 400;

		Composite searchComposite = new Composite(group, SWT.NONE);
		searchComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		searchComposite.setLayout(new GridLayout(1, true));

		Text searchField = new Text(searchComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		searchField.setMessage("Search");
		GridData searchFieldGridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		searchFieldGridData.widthHint = 180;
		searchField.setLayoutData(searchFieldGridData);

		CheckboxTreeViewer checkboxTreeViewer = new CheckboxTreeViewer(group);
		checkboxTreeViewer.getTree()
			.setLayoutData(new GridData(GridData.FILL_BOTH));
		checkboxTreeViewer.setContentProvider(new MarkerContentProvider());
		checkboxTreeViewer.setLabelProvider(new MarkerLabelProvider());
		checkboxTreeViewer.setInput("root"); //$NON-NLS-1$
		
		treeViewerWrapper = new CheckboxTreeViewerWrapper(checkboxTreeViewer);

		Map<String, RuleDescription> allMarkerDescriptions = ResolverVisitorsFactory.getAllMarkerDescriptions();
		List<String> allActiveMarkers = SimonykeesPreferenceManager.getAllActiveMarkers();
		treeViewerWrapper.populateCheckboxTreeView(allMarkerDescriptions, allActiveMarkers);
		checkboxTreeViewer.addCheckStateListener(treeViewerWrapper::createCheckListener);
		checkboxTreeViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				Comparator<MarkerItemWrapper> comparator = Comparator
					.comparing(MarkerItemWrapper::getName);
				return comparator.compare((MarkerItemWrapper) e1, (MarkerItemWrapper) e2);
			}
		});

		searchField.addModifyListener(treeViewerWrapper::createSearchFieldModifyListener);

		Composite bulkActionsComposite = new Composite(mainComposite, SWT.NONE);
		bulkActionsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		bulkActionsComposite.setLayout(new GridLayout(2, false));
		addButton(bulkActionsComposite, Messages.SimonykeesMarkersPreferencePage_enableAll, true, treeWrapper);
		addButton(bulkActionsComposite, Messages.SimonykeesMarkersPreferencePage_disableAll, false, treeWrapper);

		return mainComposite;
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
