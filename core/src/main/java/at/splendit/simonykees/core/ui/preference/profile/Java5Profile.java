package at.splendit.simonykees.core.ui.preference.profile;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.JavaVersion;

import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.ui.preference.SimonykeesPreferenceConstants;
import at.splendit.simonykees.i18n.Messages;

/**
 * A profile of all rules requiring Java 5.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class Java5Profile implements SimonykeesProfile {

	public static final String PROFILE_ID = SimonykeesPreferenceConstants.PROFILE_PREFIX + "java5"; //$NON-NLS-1$

	public Java5Profile() {
	}

	@Override
	public String getProfileId() {
		return PROFILE_ID;
	}

	@Override
	public String getProfileName() {
		return Messages.Java5Profile_profileName;
	}

	@Override
	public boolean isBuiltInProfile() {
		return true;
	}

	@Override
	public List<String> getEnabledRuleIds() {
		return RulesContainer.getAllRules().stream().filter(r -> r.getRequiredJavaVersion() == JavaVersion.JAVA_1_5)
				.map(RefactoringRule::getId).collect(Collectors.toList());
	}

}
