package at.splendit.simonykees.core.ui.preference;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jface.preference.IPreferenceStore;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.ui.preference.profile.DefaultProfile;
import at.splendit.simonykees.core.ui.preference.profile.Java8Profile;
import at.splendit.simonykees.core.ui.preference.profile.SimonykeesProfile;

/**
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class SimonykeesInitializer extends AbstractPreferenceInitializer {

	private static final List<SimonykeesProfile> DEFAULT_PROFILES = Arrays.asList(new DefaultProfile(), new Java8Profile());

	@Override
	public void initializeDefaultPreferences() {
		// FIX ME replace setValue() with setDefault()
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

		preferenceStore.setValue(SimonykeesPreferenceConstants.PROFILE_LIST, SimonykeesPreferenceManager
				.flattenArray(DEFAULT_PROFILES.stream().map(SimonykeesProfile::getProfileId).collect(Collectors.toList())));
		
		preferenceStore.setValue(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT, DEFAULT_PROFILES.get(1).getProfileId());

		/*
		 * Add default values for all rules
		 */
		for (SimonykeesProfile profile : DEFAULT_PROFILES) {
			preferenceStore.setValue(
					String.format("%s.%s", profile.getProfileId(), SimonykeesPreferenceConstants.PROFILE_NAME), //$NON-NLS-1$
					profile.getProfileName());
			preferenceStore.setValue(
					String.format("%s.%s", profile.getProfileId(), SimonykeesPreferenceConstants.PROFILE_IS_BUILT_IN), //$NON-NLS-1$
					profile.isBuiltInProfile());
			for (String ruleId : profile.getEnabledRuleIds()) {
				preferenceStore.setValue(String.format("%s.%s", profile.getProfileId(), ruleId), true); //$NON-NLS-1$
			}
		}

	}

}
