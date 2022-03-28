package eu.jsparrow.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * A simple handler to open the jSparrow Preference page.
 * 
 * @since 4.10.0
 *
 */
public class EditProfilesHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(Display.getDefault()
			.getActiveShell(), "eu.jsparrow.ui.preference.ProfilePreferencePage", null, null); //$NON-NLS-1$
		dialog.open();
		return null;
	}
}
