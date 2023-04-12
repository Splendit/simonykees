package eu.jsparrow.ui.preference;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.preference.marker.MarkerTreeViewWrapper;
import eu.jsparrow.ui.preference.profile.DefaultActiveMarkers;

/**
 * A preference page for activating and deactivating jSparrow Markers.
 * 
 * @since 4.6.0
 *
 */
public class SimonykeesMarkersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

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

		treeViewerWrapper = new MarkerTreeViewWrapper(mainComposite);

		Composite bulkActionsComposite = new Composite(mainComposite, SWT.NONE);
		bulkActionsComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		bulkActionsComposite.setLayout(new GridLayout(2, false));
		addButton(bulkActionsComposite, Messages.SimonykeesMarkersPreferencePage_enableAll, true, treeViewerWrapper);
		addButton(bulkActionsComposite, Messages.SimonykeesMarkersPreferencePage_disableAll, false, treeViewerWrapper);

		return mainComposite;
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
			DefaultActiveMarkers defaultMarkers = new DefaultActiveMarkers();
			treeViewerWrapper.selectMarkers(defaultMarkers.getActiveMarkers());
		}
	}

	public void setSearchField(String string) {		
		treeViewerWrapper.setSearchFieldText(string);
	}
}
