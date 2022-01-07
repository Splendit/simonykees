package eu.jsparrow.ui.preference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.jsparrow.core.markers.ResolverVisitorsFactory;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.ui.preference.marker.TreeWrapper;
import eu.jsparrow.ui.preference.profile.DefaultActiveMarkers;

/**
 * A preference page for activating and deactivating jSparrow Markers.
 * 
 * @since 4.6.0
 *
 */
public class SimonykeesMarkersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private final Map<String, Button> checkButtons = new HashMap<>();

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
		
		Map<String, RuleDescription> allMarkerDescriptions = ResolverVisitorsFactory.getAllMarkerDescriptions();
		List<String> allActiveMarkers = SimonykeesPreferenceManager.getAllActiveMarkers();
				
		GridData groupLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		groupLayoutData.heightHint = 400;
		ScrolledComposite scrolledComposite = new ScrolledComposite(group, SWT.V_SCROLL);
		scrolledComposite.setLayout(new GridLayout(1, false));
		scrolledComposite.setLayoutData(groupLayoutData);
		
		Composite content = new Composite(scrolledComposite, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TreeWrapper treeWrapper = new TreeWrapper(content);
		treeWrapper.init(allActiveMarkers, allMarkerDescriptions);
		
		
        // Method to minimize repetitive lines of code
        // Perhaps switch is the wrong term...
		addSwitchButton(mainComposite, "Deselect All", false, treeWrapper); //$NON-NLS-1$
		addSwitchButton(mainComposite, "Select All", true, treeWrapper); //$NON-NLS-1$
		
		scrolledComposite.setContent(content);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledComposite.setMinSize(point);

		return mainComposite;
	}
	
	protected void addActiveMarker(String markerId) {
		SimonykeesPreferenceManager.addActiveMarker(markerId);
		
		//Trying to figure out how to select all markers with the same ID
	}
	protected void removeActiveMarker(String markerId) {
		SimonykeesPreferenceManager.removeActiveMarker(markerId);
	}
	
	protected void addSwitchButton(Composite composite, String name, boolean turn, TreeWrapper treeWrapper) {
		Button thisButton = new Button(composite, SWT.PUSH);
		thisButton.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false));
		thisButton.setText(name); 
		// On switch click -> Loop treeCategories (disposed items are still in ArrayList)
		thisButton.addListener(SWT.MouseDown, event -> treeWrapper.updateAllCategoriesSelection(turn));
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		for (String marker : SimonykeesPreferenceManager.getAllActiveMarkers()) {
			SimonykeesPreferenceManager.removeActiveMarker(marker);
			Button button = checkButtons.get(marker);
			button.setSelection(false);
		}
		DefaultActiveMarkers defaultMarkers = new DefaultActiveMarkers();
		for (String marker : defaultMarkers.getActiveMarkers()) {
			SimonykeesPreferenceManager.addActiveMarker(marker);
			Button button = checkButtons.get(marker);
			button.setSelection(true);
		}
	}
}
