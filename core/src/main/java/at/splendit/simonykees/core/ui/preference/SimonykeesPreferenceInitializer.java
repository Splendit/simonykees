package at.splendit.simonykees.core.ui.preference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.ui.preference.profile.DefaultProfile;
import at.splendit.simonykees.core.ui.preference.profile.SimonykeesProfile;

/**
 * Default values for the plug-in preference page. 
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class SimonykeesPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		
		SimonykeesProfile defaultProfile = new DefaultProfile();

		String defaultProfileAsString = defaultProfile.getProfileName() + SimonykeesPreferenceConstants.NAME_RULES_DELIMITER + StringUtils.join(defaultProfile.getEnabledRuleIds(), SimonykeesPreferenceConstants.RULE_RULE_DELIMITER);
		preferenceStore.setDefault(SimonykeesPreferenceConstants.PROFILE_LIST, defaultProfileAsString);//flattenArray(

		preferenceStore.setDefault(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT,
				defaultProfile.getProfileName());

	}

}
