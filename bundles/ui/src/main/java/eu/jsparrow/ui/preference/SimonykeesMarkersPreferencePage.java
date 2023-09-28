package eu.jsparrow.ui.preference;

import java.util.Set;

import org.eclipse.jface.preference.PreferencePage;
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

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preference.marker.MarkerItemWrapperFilter;
import eu.jsparrow.ui.preference.marker.MarkerTreeViewWrapper;
import eu.jsparrow.ui.preference.profile.DefaultActiveMarkers;

/**
 * A preference page for activating and deactivating jSparrow Markers.
 * 
 * @since 4.6.0
 *
 */
public class SimonykeesMarkersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	protected Text searchField;
	private MarkerTreeViewWrapper treeViewerWrapper;

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

		GridData groupLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		groupLayoutData.heightHint = 400;
		group.setLayoutData(groupLayoutData);

		createSearchTextField(group);

		treeViewerWrapper = new MarkerTreeViewWrapper(group);

		Composite bulkActionsComposite = new Composite(mainComposite, SWT.NONE);
		bulkActionsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		bulkActionsComposite.setLayout(new GridLayout(2, false));
		addButton(bulkActionsComposite, Messages.SimonykeesMarkersPreferencePage_enableAll, true, treeViewerWrapper);
		addButton(bulkActionsComposite, Messages.SimonykeesMarkersPreferencePage_disableAll, false, treeViewerWrapper);

		return mainComposite;
	}

	protected void createSearchTextField(Group group) {
		Composite searchComposite = new Composite(group, SWT.NONE);
		searchComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		searchComposite.setLayout(new GridLayout(1, true));
		searchField = new Text(searchComposite, SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		searchField.setMessage(Messages.SimonykeesMarkersPreferencePage_searchLabelMessage);
		GridData searchFieldGridData = new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1);
		searchFieldGridData.widthHint = 180;
		searchField.setLayoutData(searchFieldGridData);
		searchField.addModifyListener(this::textRetrievalModified);
	}

	/**
	 * Method for the listener functionality for modifying text in
	 * {@link #searchField}
	 */
	protected void textRetrievalModified(ModifyEvent modifyEvent) {
		Text source = (Text) modifyEvent.getSource();
		String searchText = source.getText();
		treeViewerWrapper.setTreeViewerFilters(new MarkerItemWrapperFilter(treeViewerWrapper, searchText));
	}

	protected void addActiveMarker(String markerId) {
		SimonykeesPreferenceManager.addActiveMarker(markerId);
	}

	protected void removeActiveMarker(String markerId) {
		SimonykeesPreferenceManager.removeActiveMarker(markerId);
	}

	protected void addButton(Composite composite, String name, boolean turn, MarkerTreeViewWrapper treeWrapper) {
		Button thisButton = new Button(composite, SWT.PUSH);
		thisButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		thisButton.setText(name);
		thisButton.addListener(SWT.MouseDown, event -> treeWrapper.bulkUpdate(turn));
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		if (treeViewerWrapper != null) {
			treeViewerWrapper.selectDefaultMarkers(new DefaultActiveMarkers());
		}
	}
	
	@Override
	public boolean performOk() {
		Set<String> selectedMarkersToApply = treeViewerWrapper.getSelectedMarkersToApply();
		SimonykeesPreferenceManager.setAllActiveMarkers(selectedMarkersToApply);
		return true;
	}

	public void setSearchField(String string) {
		searchField.setText(string);
	}
}
