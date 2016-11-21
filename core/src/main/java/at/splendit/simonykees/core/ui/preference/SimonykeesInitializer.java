package at.splendit.simonykees.core.ui.preference;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.ui.preference.profile.DefaultProfile;
import at.splendit.simonykees.core.ui.preference.profile.Java8Profile;
import at.splendit.simonykees.core.ui.preference.profile.SimonykeesProfile;

/**
 * Default values for the plug-in preference page. 
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class SimonykeesInitializer extends AbstractPreferenceInitializer {

	private static final List<SimonykeesProfile> DEFAULT_PROFILES = Arrays.asList(new DefaultProfile(),
			new Java8Profile());

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

		preferenceStore.setDefault(SimonykeesPreferenceConstants.PROFILE_LIST, SimonykeesPreferenceManager.flattenArray(
				DEFAULT_PROFILES.stream().map(SimonykeesProfile::getProfileId).collect(Collectors.toList())));

		preferenceStore.setDefault(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT,
				DEFAULT_PROFILES.get(0).getProfileId());

		/*
		 * Add default values for all default rules
		 */
		for (SimonykeesProfile profile : DEFAULT_PROFILES) {
			preferenceStore.setDefault(SimonykeesPreferenceManager.getProfileNameKey(profile.getProfileId()),
					profile.getProfileName());
			preferenceStore.setDefault(SimonykeesPreferenceManager.getProfileBuiltInKey(profile.getProfileId()),
					profile.isBuiltInProfile());
			
			// set rules as default according to the profile specific enabled rules
			for (String ruleId : profile.getEnabledRuleIds()) {
				preferenceStore.setDefault(
						SimonykeesPreferenceManager.getProfileRuleKey(profile.getProfileId(), ruleId), true);
			}
		}

	}

}
