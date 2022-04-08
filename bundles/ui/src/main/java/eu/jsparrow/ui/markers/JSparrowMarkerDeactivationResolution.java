package eu.jsparrow.ui.markers;

import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.NAME_KEY;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.dialogs.PreferencesUtil;

import eu.jsparrow.ui.preference.SimonykeesMarkersPreferencePage;

public class JSparrowMarkerDeactivationResolution implements IMarkerResolution2 {

	private String name;

	public JSparrowMarkerDeactivationResolution(IMarker marker) {
		name = marker.getAttribute(NAME_KEY, "Deactivate"); //$NON-NLS-1$
	}

	@Override
	public String getLabel() {
		return "Deactivate " + name;
	}

	@Override
	public void run(IMarker marker) {
		Shell activeShell = Display.getDefault()
				.getActiveShell();
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
				activeShell, "eu.jsparrow.ui.preference.MarkersPreferencePage", null, null); //$NON-NLS-1$
		SimonykeesMarkersPreferencePage page = (SimonykeesMarkersPreferencePage) dialog.getSelectedPage();
		page.setSearchField(name);
		dialog.open();

	}

	@Override
	public String getDescription() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return JSparrowImages.JSPARROW_ACTIVE_16;
	}

}
