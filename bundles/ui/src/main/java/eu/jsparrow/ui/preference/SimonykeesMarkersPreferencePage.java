package eu.jsparrow.ui.preference;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.jsparrow.core.markers.ResolverVisitorsFactory;

public class SimonykeesMarkersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		// required by the parent
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, true);
		composite.setLayoutData(gd);
		composite.setLayout(new GridLayout(1, true));
		
		Set<String> allMarkerIds = ResolverVisitorsFactory.getAllMarkerIds();
		List<String> allActiveMarkers = SimonykeesPreferenceManager.getAllActiveMarkers();
		
		for(String markerId : allMarkerIds) {
			Group group = new Group(composite, SWT.NONE);
			group.setLayout(new GridLayout(2, true));
			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Button button = new Button(group, SWT.CHECK);
			button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			button.setVisible(true);
			
			button.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Button source = (Button) e.getSource();
					if(source.getSelection()) {
						SimonykeesPreferenceManager.addActiveMarker(markerId);
					} else {
						SimonykeesPreferenceManager.removeActiveMarker(markerId);
					}
				}
			});
			boolean selection = allActiveMarkers.contains(markerId);
			button.setSelection(selection);
			Label label = new Label(group, SWT.NONE);
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			label.setText(markerId);
			label.setVisible(true);
		}
		
		return composite;
	}

}
