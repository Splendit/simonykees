package eu.jsparrow.ui.preference;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import eu.jsparrow.core.markers.ResolverVisitorsFactory;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RuleDescription;

public class SimonykeesMarkersPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	@Override
	public void init(IWorkbench workbench) {
		// required by the parent
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		composite.setLayoutData(gd);
		composite.setLayout(new GridLayout(1, true));
		
		Group group = new Group(composite, SWT.NONE);
		group.setText(Messages.SimonykeesMarkersPreferencePage_jSparrowMarkersGroupText);
		group.setLayout(new GridLayout(1, false));
		GridData groupLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		groupLayoutData.heightHint = 400;
		group.setLayoutData(groupLayoutData);
		
		Map<String, RuleDescription> allMarkerDescriptions = ResolverVisitorsFactory.getAllMarkerDescriptions();
		List<String> allActiveMarkers = SimonykeesPreferenceManager.getAllActiveMarkers();
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(group, SWT.V_SCROLL);
		scrolledComposite.setLayout(new GridLayout(1, false));
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite content = new Composite(scrolledComposite, SWT.NONE);
		content.setLayout(new GridLayout(2, false));
		GridData contentGD = new GridData(GridData.FILL_HORIZONTAL);
		content.setLayoutData(contentGD);
		
		

		
		for(Map.Entry<String, RuleDescription> entry : allMarkerDescriptions.entrySet()) {
			String markerId = entry.getKey();
			RuleDescription description = entry.getValue();
			Button button = new Button(content, SWT.CHECK);
			button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
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
			Label label = new Label(content, SWT.NONE);
			label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			label.setText(description.getName());
			label.setVisible(true);
		}
		
		scrolledComposite.setContent(content);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		Point point = content.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledComposite.setMinSize(point);
		
		return composite;
	}

}
