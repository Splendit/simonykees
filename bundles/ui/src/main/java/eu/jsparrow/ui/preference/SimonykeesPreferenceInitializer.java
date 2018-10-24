package eu.jsparrow.ui.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import eu.jsparrow.ui.Activator;

/**
 * Default values for the plug-in preference page.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class SimonykeesPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Activator.getDefault()
			.getPreferenceStore();

		preferenceStore.setDefault(SimonykeesPreferenceConstants.PROFILE_LIST,
				SimonykeesPreferenceManager.getDefaultProfileList());

		preferenceStore.setDefault(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT,
				SimonykeesPreferenceManager.getDefaultProfileName());

		preferenceStore.setDefault(SimonykeesPreferenceConstants.ENABLE_INTRO, true);

		preferenceStore.setDefault(SimonykeesPreferenceConstants.ENABLE_DASHBOARD, true);

		preferenceStore.setDefault(SimonykeesPreferenceConstants.RESOLVE_PACKAGES_RECURSIVELY, true);
	}
}
